package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.SendLetterChecksService;

@Component
@Slf4j
public class SendLetterChecksTrigger implements Trigger {
    private final SendLetterChecksService sendLetterChecksService;

    public SendLetterChecksTrigger(SendLetterChecksService sendLetterChecksService) {
        this.sendLetterChecksService = sendLetterChecksService;
    }

    @Override
    public void trigger() {
        sendLetterChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.SEND_LETTER_CHECKS.equals(scheduleTypes);
    }
}
