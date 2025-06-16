package uk.gov.hmcts.reform.bsp.models;

import lombok.Getter;
import uk.gov.hmcts.reform.bsp.triggers.BulkPrintChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.BulkScanChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

/**
 * This class contains the expected types of schedules that could be run.
 */
@Getter
public enum ScheduleTypes {
    BULK_SCAN_CHECKS(BulkScanChecksTrigger.class),
    BULK_PRINT_CHECKS(BulkPrintChecksTrigger.class);

    private final Class<? extends Trigger> triggerClass;

    ScheduleTypes(Class<? extends Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }
}
