package goleap.ai.boundless.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.google.common.flogger.FluentLogger;
import goleap.ai.boundless.db.InvoicesRepository;
import goleap.ai.boundless.model.Invoices.Extract;
import goleap.ai.boundless.model.Invoices.Invoice;
import goleap.ai.boundless.model.Invoices.PdfSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class InvoicesProcessor {
    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

    @Value("${invoices.upload.location}")
    protected String uploadLocation;

    private final ObjectMapper objectMapper;
    private final PdfAPIHandler pdfApiHandler;
    private final InvoicesRepository invoicesRepository;

    @Autowired
    public InvoicesProcessor(
            ObjectMapper objectMapper, PdfAPIHandler pdfApiHandler, InvoicesRepository invoicesRepository) {
        this.objectMapper = objectMapper;
        this.pdfApiHandler = pdfApiHandler;
        this.invoicesRepository = invoicesRepository;
    }

    private static void validate(String accountId, MultipartFile file) {
        if (!StringUtils.hasLength(accountId)) {
            throw new RuntimeException("Missing account.");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
    }

    private CompletableFuture<Optional<Invoice>> extractData(String accountId, String sourceId) {
        CompletableFuture<String> dataF = pdfApiHandler.extractPdfData(sourceId);
        return dataF.thenApply(data -> parseExtractData(data, accountId, sourceId));
    }

    private String sanitize(String body) {
        String sanitized = body.substring(body.indexOf("{"), body.indexOf('}') + 1);
        LOG.atInfo().log("Sanitized JSON=[%s]", sanitized);
        return sanitized;
    }

    private Optional<Invoice> parseExtractData(String data, String accountId, String sourceId) {
        try {
            var extract = objectMapper.readValue(data, Extract.class);
            var invoice = objectMapper.readValue(sanitize(extract.content()), Invoice.class);
            invoice.setAccountId(accountId);
            invoice.setPdfSourceId(sourceId);
            return Optional.of(invoice);
        } catch (Exception e) {
            LOG.atSevere().withCause(e).log();
        }
        return Optional.empty();
    }

    private Boolean storeData(Invoice invoiceData, MultipartFile multipartFile) {
        var hash = getHash(multipartFile);
        if (hash.isPresent()) {
            invoiceData.setId(hash.get());
            LOG.atInfo().log("Saving invoice [%s]", invoiceData.toString());
            invoicesRepository.save(invoiceData);
            return true;
        } else {
            LOG.atSevere().log("Failed to store file %s", multipartFile.getOriginalFilename());
            return false;
        }
    }

    @Async
    public CompletableFuture<Void> uploadAndProcessPdf(String accountId, MultipartFile multipartFile) {
        validate(accountId, multipartFile);

        boolean exists = validateFileExists(multipartFile);
        if (!exists) {
            CompletableFuture<String> sourceIdJsonF = pdfApiHandler.uploadPdf(multipartFile);
            CompletableFuture<Boolean> storedDataF = sourceIdJsonF.thenCompose(sourceIdJson -> {
                PdfSource pdfSource = parsePdfSource(sourceIdJson);
                CompletableFuture<Optional<Invoice>> invoiceOptF = extractData(accountId, pdfSource.sourceId());
                return invoiceOptF.thenCompose(invoiceOpt -> {
                    if (invoiceOpt.isPresent()) {
                        return CompletableFuture.completedFuture(storeData(invoiceOpt.get(), multipartFile));
                    } else {
                        return CompletableFuture.failedFuture(new RuntimeException(
                                "Failed to extract data from " + multipartFile.getOriginalFilename()));
                    }
                });
            });
            CompletableFuture<Path> storeFileF = storeFileLocally(accountId, multipartFile);
            return CompletableFuture.allOf(storedDataF, storeFileF);
        } else {
            return CompletableFuture.failedFuture(
                    new RuntimeException(multipartFile.getOriginalFilename() + " exists "));
        }
    }

    private PdfSource parsePdfSource(String sourceIdJson) {
        try {
            return objectMapper.readValue(sourceIdJson, PdfSource.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateFileExists(MultipartFile multipartFile) {
        var hash = getHash(multipartFile);
        if (hash.isPresent()) {
            return invoicesRepository.existsById(hash.get());
        } else {
            LOG.atSevere().log("Failed to validate file existence for %s", multipartFile.getOriginalFilename());
            return false;
        }
    }

    private Optional<String> getHash(MultipartFile multipartFile) {
        try {
            return Optional.of(
                    UuidCreator.getNameBasedMd5(multipartFile.getBytes()).toString());
        } catch (Exception e) {
            LOG.atSevere().withCause(e).log();
            return Optional.empty();
        }
    }

    @Async
    public CompletableFuture<Path> storeFileLocally(String accountId, MultipartFile file) {
        validate(accountId, file);
        String path = uploadLocation + File.separator + accountId;
        var rootLocation = Paths.get(path);
        try {
            if (!Files.exists(rootLocation)) Files.createDirectory(rootLocation);
            Path destinationFile = rootLocation
                    .resolve(Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize()
                    .toAbsolutePath();
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return CompletableFuture.completedFuture(destinationFile);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public void init() {
        try {
            var rootLocation = Paths.get(uploadLocation);
            if (!Files.exists(rootLocation)) Files.createDirectory(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<Invoice> loadInvoices() {
        List<Invoice> result = new ArrayList<>();
        invoicesRepository.findAll().forEach(result::add);
        return result;
    }
}
