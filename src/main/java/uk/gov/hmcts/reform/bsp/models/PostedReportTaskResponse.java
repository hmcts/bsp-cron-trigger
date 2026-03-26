package uk.gov.hmcts.reform.bsp.models;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PostedReportTaskResponse {
    final String reportCode;
    final LocalDate reportDate;
    final boolean international;

    long markedPostedCount = 0;

    boolean processingFailed = false;
    String errorMessage = null;
}
