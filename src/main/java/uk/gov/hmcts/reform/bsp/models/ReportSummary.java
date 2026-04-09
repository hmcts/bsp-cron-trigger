package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReportSummary {

    @JsonProperty("received")
    private int received;

    @JsonProperty("rejected")
    private int rejected;

    @JsonProperty("container")
    private String container;

    @JsonProperty("date")
    private String date;
}
