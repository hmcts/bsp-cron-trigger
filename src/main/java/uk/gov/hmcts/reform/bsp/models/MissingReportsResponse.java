package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MissingReportsResponse {
    @JsonProperty("service_name")
    final String serviceName;

    @JsonProperty("is_international")
    final boolean isInternational;

    @JsonProperty("report_date")
    final LocalDate reportDate;
}
