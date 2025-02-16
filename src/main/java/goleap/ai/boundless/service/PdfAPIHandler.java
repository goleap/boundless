package goleap.ai.boundless.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.flogger.FluentLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static goleap.ai.boundless.service.Constants.DATE_FORMAT;

@Service
public class PdfAPIHandler {
    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

    private static final String PROMPT =
                """
                Extract all entities and their values from the uploaded PDF invoice or purchase order and return as JSON with the following instructions and without any content message:
                - Ensure all field names are in camelCase
                - All amounts should return the value and corresponding currency in individual fields within a JSON object. For eg: If we have "Subtotal: ￥160,470.0", then the output should be:
                {
                    "subtotal": {
                        "value": 160470,
                        "currency": "RMB"
                    }
                }
                - field names should not have any currency information in them
                - All date values need to be output in the format %s
                - Seller and buyer name and address should be part of seller and buyer JSON objects name, address, and contact fields
                - All order items should be part of a json array named items. Additionally, each item should have the following fields - id or number or sku name and/or description, quantity, amount per unit, total amount for that line item. All additional information per line item, put it under "additional" JSON object of key-value pairs
                - All phone number fields should either be named "phone" or have the suffix "phone"
                - Ensure all bank deposit information is extracted as well under bank_details with beneficiary info
                - Ensure Invoice & Purchase Order numbers and dates are correctly mapped, like the following:
                -- Map any invoice number to the field invoice_number
                -- Map any purchase order number to the field po_number
                -- Map any invoice or purchase order date to the field "date"
                """;

    private static final String MESSAGE =
            """
                    {
                        "sourceId": "%s",
                        "messages": [
                            {
                                "role": "user",
                                "content": %s
                            }
                        ]
                    }
                    """;

    @Value("${pdf.upload.url}")
    protected String pdfUploadUrl;

    @Value("${pdf.chats.url}")
    protected String pdfChatsUrl;

    @Value("${pdf.api.key.name}")
    protected String pdfApiKeyName;

    @Value("${pdf.api.key.value}")
    protected String pdfApiKeyValue;

    private final RetryTemplate retryTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PdfAPIHandler(RetryTemplate retryTemplate, ObjectMapper objectMapper) {
        this.retryTemplate = retryTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<String> uploadPdf(MultipartFile multipartFile) {
        Objects.requireNonNull(multipartFile);

        HttpHeaders headers = new HttpHeaders();
        headers.add(pdfApiKeyName, pdfApiKeyValue);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", multipartFile.getResource());

        HttpEntity<Object> requestEntity = new HttpEntity<>(map, headers);

        return makeAPICall(pdfUploadUrl, requestEntity, multipartFile.getOriginalFilename());
    }

    @Async
    public CompletableFuture<String> extractPdfData(String sourceId) {
        Objects.requireNonNull(sourceId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(pdfApiKeyName, pdfApiKeyValue);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        try {
            String prompt = String.format(PROMPT, DATE_FORMAT);
            String jsonPrompt = objectMapper.writeValueAsString(prompt);
            String message = String.format(MESSAGE, sourceId, jsonPrompt);
            HttpEntity<Object> requestEntity = new HttpEntity<>(message, headers);

            LOG.atInfo().log("Message -> [%s]", message);
            return makeAPICall(pdfChatsUrl, requestEntity, sourceId, message);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<String> makeAPICall(String url, HttpEntity<Object> requestEntity, String... params) {
        return retryTemplate.execute(ctx -> {
            LOG.atInfo().log("Posting to [%s] - retry [%d]", url, ctx.getRetryCount());
            RestTemplate restTemplate = new RestTemplate();
            // execute HTTP call
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOG.atInfo().log(
                    "Received code=[%s], response=[%s]", responseEntity.getStatusCode(), responseEntity.getBody());

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(Objects.requireNonNull(responseEntity.getBody()));
            } else {
                return CompletableFuture.failedFuture(new RuntimeException("Error processing data for " + url + " "
                        + Arrays.toString(params) + " " + responseEntity.getBody()));
            }
        });
    }
}
