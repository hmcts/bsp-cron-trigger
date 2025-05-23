package uk.gov.hmcts.reform.bsp.triggers;

import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;

/**
 * Interface for the logic for each trigger.
 */
public interface Trigger {

    void trigger();

    boolean isApplicable(ScheduleTypes scheduleTypes);
}
