package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class EnvelopeInfo {

    @JsonProperty("container")
    private String container;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("envelope_id")
    private UUID envelopeId;

    @JsonProperty("created_at")
    private Instant createdAt;
}
