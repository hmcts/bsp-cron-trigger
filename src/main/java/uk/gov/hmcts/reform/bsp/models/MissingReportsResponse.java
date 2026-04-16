package uk.gov.hmcts.reform.bsp.models;

import lombok.Data;

@Data
public class MissingReportsResponse {
    final String serviceName;
    final boolean international;
}
