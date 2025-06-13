package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("last_modified")
    private Instant lastModified;

    @JsonProperty("document_control_number")
    private String documentControlNumber;
}
