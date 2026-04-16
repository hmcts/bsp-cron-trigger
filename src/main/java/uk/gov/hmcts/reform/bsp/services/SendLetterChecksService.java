package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.MissingReportsResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SendLetterChecksService {

    private final SendLetterServiceClient sendLetterServiceClient;
    private final SlackMessageHelper slackHelper;

    public SendLetterChecksService(
        SendLetterServiceClient sendLetterServiceClient,
        SlackMessageHelper slackHelper
    ) {
        this.sendLetterServiceClient = sendLetterServiceClient;
        this.slackHelper = slackHelper;
    }

    /**
     * Entry point to run send letter service checks.
     */
    public void runDailyChecks() {
        Optional<String> action = checkMissingReports();

        sendSlackSummary(action);
    }

    /**
     * Checks that there are no missing reports in the last 7 days.
     * @return Record of missing reports, if any
     */
    private Optional<String> checkMissingReports() {
        try {
            LocalDate today = LocalDate.now();
            List<MissingReportsResponse> missingReports =
                sendLetterServiceClient.runCheckReports(today.toString(), today.minusDays(7).toString());
            if (!missingReports.isEmpty()) {
                return Optional.of(
                    "Missing reports found: " + missingReports + ". Check App insights for details.");
            }
        } catch (Exception e) {
            log.error("Error while checking for missing reports", e);
            return Optional.of("Failed to check for missing reports. Check App insights.");
        }
        return Optional.empty();
    }

    /**
     * Sends a summary of Send letter service checks to Slack.
     * @param action Action description to include in the summary
     */
    private void sendSlackSummary(Optional<String> action) {
        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder sb = new StringBuilder(
            String.format("*:mag: Send Letter Service Daily Check (%s)*\n", timestamp)
        );
        if (action.isEmpty()) {
            sb.append("> ✅ All clear! No Send Letter Service issues detected. :tada:");
        } else {
            sb.append("> ❗ Send Letter Service issue found:\n");
            sb.append("• ").append(action.get()).append("\n");
        }
        slackHelper.sendLongMessage(sb.toString());
    }
}
