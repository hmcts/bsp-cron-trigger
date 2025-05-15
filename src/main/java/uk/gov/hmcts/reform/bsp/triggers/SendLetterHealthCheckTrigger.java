package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;

/**
 * This is an example and should be removed when the first real trigger is created.
 */
@Service
@Slf4j
public class SendLetterHealthCheckTrigger implements Trigger {

    private final SendLetterServiceClient sendLetterServiceClient;

    public SendLetterHealthCheckTrigger(SendLetterServiceClient sendLetterServiceClient) {
        this.sendLetterServiceClient = sendLetterServiceClient;
    }

    @Override
    public void trigger() {
        if (sendLetterServiceClient.getHealth().contains("UP")) {
            log.info("Send letter health check passing");
        } else {
            log.error("Send letter health check failed");
        }
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.SEND_LETTER_HEALTH_CHECK.equals(scheduleTypes);
    }
}
