package uk.gov.hmcts.reform.bsp.models;

import lombok.Getter;
import uk.gov.hmcts.reform.bsp.triggers.BsDailyChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.BpDailyChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

/**
 * This class contains the expected types of schedules that could be run.
 */
@Getter
public enum ScheduleTypes {
    BULK_SCAN_CHECKS(BsDailyChecksTrigger.class),
    BULK_PRINT_CHECKS(BpDailyChecksTrigger.class);

    private final Class<? extends Trigger> triggerClass;

    ScheduleTypes(Class<? extends Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }
}
