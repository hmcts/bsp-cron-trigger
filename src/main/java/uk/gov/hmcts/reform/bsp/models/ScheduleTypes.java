package uk.gov.hmcts.reform.bsp.models;

import uk.gov.hmcts.reform.bsp.triggers.SendLetterHealthCheckTrigger;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

/**
 * This class contains the expected types of schedules that could be run.
 */
public enum ScheduleTypes {
    SEND_LETTER_HEALTH_CHECK(SendLetterHealthCheckTrigger.class);

    private final Class<? extends Trigger> triggerClass;

    ScheduleTypes(Class<? extends Trigger> triggerClass) {
        this.triggerClass = triggerClass;
    }

    public Class<? extends Trigger> getTriggerClass() {
        return triggerClass;
    }
}
