package uk.gov.hmcts.reform.bsp.models;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PostedReportTaskResponse {
    final String serviceName;
    final LocalDate reportDate;
    final long markedPostedCount;
    boolean processingFailed = false;
    String errorMessage = null;
}
