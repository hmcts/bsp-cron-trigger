package uk.gov.hmcts.reform.bsp.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkPrintProcessingProperties {
    Duration processedReportsRetrievalWindow = Duration.ofMinutes(45);
}
