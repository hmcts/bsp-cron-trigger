package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.DailyChecksService;

@Component
@Slf4j
public class DailyChecksTrigger implements Trigger {
    private final DailyChecksService dailyChecksService;

    public DailyChecksTrigger(DailyChecksService dailyChecksService) {
        this.dailyChecksService = dailyChecksService;
    }

    @Override
    public void trigger() {
        dailyChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BS_DAILY_CHECKS.equals(scheduleTypes);
    }
}
