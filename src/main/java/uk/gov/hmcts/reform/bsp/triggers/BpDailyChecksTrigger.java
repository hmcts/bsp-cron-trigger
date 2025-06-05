package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BpDailyChecksService;

/**
 * This is an example and should be removed when the first real trigger is created.
 */
@Component
@Slf4j
public class BpDailyChecksTrigger implements Trigger {

    private final BpDailyChecksService bpDailyChecksService;

    public BpDailyChecksTrigger(BpDailyChecksService bpDailyChecksService) {
        this.bpDailyChecksService = bpDailyChecksService;
    }

    @Override
    public void trigger() {
        bpDailyChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BP_DAILY_CHECKS.equals(scheduleTypes);
    }
}
