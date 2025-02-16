package goleap.ai.boundless;

import com.google.common.flogger.FluentLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoiceAPIControllerIT {
    private static final FluentLogger LOG = FluentLogger.forEnclosingClass();

    @Autowired
    private TestRestTemplate template;

    private void uploadInvoice(String fileName) {
        ClassPathResource resource = new ClassPathResource(fileName, getClass());
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);

        var url = "/v1/upload";
        ResponseEntity<String> response = template.exchange(url, HttpMethod.POST, new HttpEntity<>(map), String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void uploadInvoice() {
        uploadInvoice("PO-SMKD20220830001（INVOICE）.pdf");
    }

    @Test
    public void getInvoices() {
        uploadInvoice("invoiceBJ20220830008.pdf");
        var url = "/v1/invoices";
        ResponseEntity<String> response = template.getForEntity(url, String.class);
        assertNotNull(response.getBody());
        LOG.atInfo().log(response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
