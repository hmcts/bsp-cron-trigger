package uk.gov.hmcts.reform.bsp.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StaleLetter {

    public final UUID id;
    public final String status;
    public final String service;

    @JsonProperty("created_at")
    public final LocalDateTime createdAt;

    @JsonProperty("sent_to_print_at")
    public final LocalDateTime sentToPrintAt;

    /**
     * Constructor here is needed, because the two variables above are final
     */
    public StaleLetter(UUID id, String status, String service,
                       LocalDateTime createdAt, LocalDateTime sentToPrintAt) {
        this.id = id;
        this.status = status;
        this.service = service;
        this.createdAt = createdAt;
        this.sentToPrintAt = sentToPrintAt;
    }
}
