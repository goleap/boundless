package goleap.ai.boundless.web;

import com.google.common.flogger.FluentLogger;
import goleap.ai.boundless.model.IncidentBriefs.Result;
import goleap.ai.boundless.service.IncidentBriefsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.CompletableFuture;

import static goleap.ai.boundless.service.Constants.DEFAULT_ACCOUNT;

@Controller
public class IncidentBriefAPIController {
    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

    private final IncidentBriefsProcessor processor;

    @Autowired
    public IncidentBriefAPIController(IncidentBriefsProcessor processor) {
        this.processor = processor;
    }

    @PostMapping(path = "/v1/upload")
    public String uploadAndProcessPdf(@RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) String accountId,
                                      RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasLength(accountId)) {
            accountId = DEFAULT_ACCOUNT;
        }
        LOG.atInfo().log("Received POST account=[%s]", accountId);
        try {
            CompletableFuture<Void> processedF = processor.uploadAndProcessPdf(accountId, file);
            processedF.join();
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded " + file.getOriginalFilename() + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            LOG.atSevere().withCause(e).log();
        }
        return "redirect:/";
    }

    @GetMapping(path = "/v1/incident-briefs", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Result> getData(@RequestParam(required = false) String accountId) {
        try {
            if (!StringUtils.hasLength(accountId)) {
                accountId = DEFAULT_ACCOUNT;
            }
            LOG.atInfo().log("Received GET account=[%s]", accountId);
            var result = processor.loadIncidentBriefs();
            return ResponseEntity.status(HttpStatus.OK).body(new Result(result.size(), 0, -1, result));
        } catch (Exception e) {
            LOG.atSevere().withCause(e).log();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Result(0, 0, -1, null));
    }

    @GetMapping("/")
    public String listIncidentBriefs(Model model) {
        try {
            var result = processor.loadIncidentBriefs();
            model.addAttribute("incidentBriefs", result);
        } catch (Exception e) {
            LOG.atSevere().withCause(e).log();
        }
        return "index";
    }
}
