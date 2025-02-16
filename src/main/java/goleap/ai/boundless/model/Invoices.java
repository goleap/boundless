package goleap.ai.boundless.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

public interface Invoices {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Entity
    @Table(name = "invoices")
    class Invoice {
        @Id
        @JsonProperty
        private String id;

        @JsonProperty("invoiceNumber")
        private String invoiceNumber;

        @JsonProperty("invoiceTotal")
        private String total;

        @JsonProperty("sellerName")
        private String seller;

        @JsonProperty("buyerName")
        private String buyer;

        @JsonProperty
        private Date date;

        private String pdfSourceId;
        private String accountId;
    }

    record Result(
            int total,
            int from,
            int limit,
            List<Invoice> invoices) {
    }

    record PdfSource(String sourceId) {
    }

    record Extract(String content) {
    }
}
