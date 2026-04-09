package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.XbpChecksService;

@Component
@Slf4j
public class XbpChecksTrigger implements Trigger {
    private final XbpChecksService xbpChecksService;

    public XbpChecksTrigger(XbpChecksService xbpChecksService) {
        this.xbpChecksService = xbpChecksService;
    }

    @Override
    public void trigger() {
        xbpChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.XBP_CHECKS.equals(scheduleTypes);
    }
}
