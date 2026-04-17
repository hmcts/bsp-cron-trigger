package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MissingReportsResponse {
    @JsonProperty("serviceName")
    final String serviceName;

    @JsonProperty("isInternational")
    final boolean isInternational;
}
