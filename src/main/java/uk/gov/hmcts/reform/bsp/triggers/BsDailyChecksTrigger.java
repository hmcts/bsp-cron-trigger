package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BsDailyChecksService;

@Component
@Slf4j
public class BsDailyChecksTrigger implements Trigger {
    private final BsDailyChecksService bsDailyChecksService;

    public BsDailyChecksTrigger(BsDailyChecksService bsDailyChecksService) {
        this.bsDailyChecksService = bsDailyChecksService;
    }

    @Override
    public void trigger() {
        bsDailyChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BS_DAILY_CHECKS.equals(scheduleTypes);
    }
}
