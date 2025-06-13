package uk.gov.hmcts.reform.bsp.models;

public enum Status {
    CREATED,
    METADATA_FAILURE,
    UPLOADED,
    UPLOAD_FAILURE,
    NOTIFICATION_SENT,
    ABORTED,
    COMPLETED
}
