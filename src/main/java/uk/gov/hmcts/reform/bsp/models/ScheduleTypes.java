package uk.gov.hmcts.reform.bsp.models;

import lombok.Getter;
import uk.gov.hmcts.reform.bsp.triggers.DailyChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.SendLetterHealthCheckTrigger;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

/**
 * This class contains the expected types of schedules that could be run.
 */
@Getter
public enum ScheduleTypes {
    BSP_DAILY_CHECKS(DailyChecksTrigger.class),
    SEND_LETTER_HEALTH_CHECK(SendLetterHealthCheckTrigger.class);

    private final Class<? extends Trigger> triggerClass;

    ScheduleTypes(Class<? extends Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }
}
