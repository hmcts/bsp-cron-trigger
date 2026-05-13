package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.MissingReportsChecksService;

@Component
@Slf4j
public class MissingReportsChecksTrigger implements Trigger {
    private final MissingReportsChecksService missingReportsChecksService;

    public MissingReportsChecksTrigger(MissingReportsChecksService missingReportsChecksService) {
        this.missingReportsChecksService = missingReportsChecksService;
    }

    @Override
    public void trigger() {
        missingReportsChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.MISSING_REPORTS_CHECKS.equals(scheduleTypes);
    }
}
