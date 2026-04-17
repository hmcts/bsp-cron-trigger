package uk.gov.hmcts.reform.bsp.integrations;

import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class SlackMessageHelper {
    protected static final int MAX_CHUNK = 4000;
    protected static final int MAX_TOTAL = 20_000;

    private final SlackClient slackClient;

    public SlackMessageHelper(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    /**
     * Splits a long message into 4k‐char chunks and sends each in turn,
     * unless the total length exceeds 20k. In which case a single
     * “too large” warning message is sent instead.
     */
    public void sendLongMessage(String message) {
        int length = message.length();

        if (length > MAX_TOTAL) {
            slackClient.sendSlackMessage(
                String.format(
                    "*:warning: Message is too large to send (%d chars; limit is %d)*",
                    length, MAX_TOTAL
                )
            );
            return;
        }

        int start = 0;
        while (start < length) {
            int end = Math.min(start + MAX_CHUNK, length);
            slackClient.sendSlackMessage(message.substring(start, end));
            start = end;
        }
    }

    /**
     * Sends a standardized daily check summary to Slack.
     * @param serviceName Name of the service being checked
     * @param icon Icon to display in the header
     * @param action Optional action description if an issue is found
     */
    public void sendDailyCheckSummary(String serviceName, String icon, Optional<String> action) {
        sendDailyCheckSummary(serviceName, icon, action.map(List::of).orElse(List.of()));
    }

    /**
     * Sends a standardized daily check summary to Slack with multiple actions.
     * @param serviceName Name of the service being checked
     * @param icon Icon to display in the header
     * @param actions List of action descriptions if issues are found
     */
    public void sendDailyCheckSummary(String serviceName, String icon, List<String> actions) {
        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder sb = new StringBuilder(
            String.format("*%s %s Daily Check (%s)*\n", icon, serviceName, timestamp)
        );

        if (actions.isEmpty()) {
            sb.append(String.format("> ✅ All clear! No %s issues detected. :tada:", serviceName));
        } else {
            sb.append(String.format("> ❗ %s issue found:\n", serviceName));
            actions.forEach(a -> sb.append("• ").append(a).append("\n"));
        }

        sendLongMessage(sb.toString());
    }
}
