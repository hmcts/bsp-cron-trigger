package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;

import java.util.List;

@Service
@Slf4j
public class BulkScanChecksService {
    private final SlackClient slackClient;

    public BulkScanChecksService(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    public void runDailyChecks() {
        List<String> actions = List.of(
            "Look at bulk scan envelope that cannot be reprocessed.",
            "Send stand-up summary"
        );

        StringBuilder sb = new StringBuilder("*:spiral_note_pad: Today's Bulk Scan Actions:*\n");
        for (String action : actions) {
            sb.append("• ").append(action).append("\n");
        }

        slackClient.sendSlackMessage(sb.toString());
    }
}
