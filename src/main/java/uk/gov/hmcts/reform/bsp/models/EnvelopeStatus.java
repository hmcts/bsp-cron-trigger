package uk.gov.hmcts.reform.bsp.models;

public enum EnvelopeStatus {
    CREATED,
    METADATA_FAILURE,
    UPLOADED,
    UPLOAD_FAILURE,
    NOTIFICATION_SENT,
    ABORTED,
    COMPLETED
}
