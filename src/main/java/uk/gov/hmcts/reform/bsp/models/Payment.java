package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Payment {

    private UUID id;
    private String envelopeId;
    private String ccdReference;

    @JsonProperty("exceptionRecord")
    private boolean isExceptionRecord;

    private String poBox;
    private String jurisdiction;
    private String service;
    private String statusMessage;
    private List<String> payments;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
