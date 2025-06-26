package uk.gov.hmcts.reform.bsp.integrations;

import org.springframework.stereotype.Component;

@Component
public class SlackMessageHelper {
    private static final int MAX_CHUNK = 4000;
    private static final int MAX_TOTAL = 20_000;

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
}
