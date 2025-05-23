package uk.gov.hmcts.reform.bsp.triggers;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;

import java.io.IOException;

@Service
@Slf4j
public class BspDailyChecksTrigger implements Trigger {

    public BspDailyChecksTrigger() {
    }

    @Override
    public void trigger() {
        try {
            Slack.getInstance()
                .methods(System.getenv("SLACK_TOKEN"))
                .chatPostMessage(req -> req
                    .channel(System.getenv("SLACK_CHANNEL_TOKEN"))
                    .text(":wave: Hi from a bot written in Java!")
                );
        } catch (IOException | SlackApiException ex) {
            log.error(ex.getMessage());
            log.error("Exception occurred while calling Slack API", ex);
        }
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BSP_DAILY_CHECKS.equals(scheduleTypes);
    }
}
