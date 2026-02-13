package uk.gov.hmcts.reform.bsp.models;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PostedReportTaskResponse {
    final String reportCode;
    final LocalDate reportDate;
    final boolean isInternational;

    long markedPostedCount = 0;

    boolean processingFailed = false;
    String errorMessage = null;
}
