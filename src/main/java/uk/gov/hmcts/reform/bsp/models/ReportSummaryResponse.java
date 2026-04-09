package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReportSummaryResponse {
    @JsonProperty("data")
    private List<ReportSummary> data;

    @JsonProperty("total_received")
    private int totalReceived;

    @JsonProperty("total_rejected")
    private int totalRejected;

    @JsonProperty("time_stamp")
    private String timeStamp;
}
