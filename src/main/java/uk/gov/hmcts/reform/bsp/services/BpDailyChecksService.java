package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;

import java.util.List;

@Service
@Slf4j
public class BpDailyChecksService {
    private final SlackClient slackClient;

    public BpDailyChecksService(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    public void runDailyChecks() {
        List<String> actions = List.of(
            "Look at bulk print letter that cannot be aborted.",
            "Send stand-up summary"
        );

        StringBuilder sb = new StringBuilder("*:spiral_note_pad: Today's Bulk Print Actions:*\n");
        for (String action : actions) {
            sb.append("• ").append(action).append("\n");
        }

        slackClient.sendSlackMessage(sb.toString());
    }
}
