package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdatePayment {
    private UUID id;

    @JsonProperty("envelopeId")
    private String envelopeId;

    private String jurisdiction;
    private String exceptionRecordRef;
    private String newCaseRef;
    private String statusMessage;
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
