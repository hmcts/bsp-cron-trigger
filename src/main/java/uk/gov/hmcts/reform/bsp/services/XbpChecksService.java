package uk.gov.hmcts.reform.bsp.services;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.feign.BankHolidayClient;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.BankHolidays;
import uk.gov.hmcts.reform.bsp.models.ReportSummaryResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XbpChecksService {

    private final BlobRouterServiceClient blobClient;
    private final BankHolidayClient bankHolidayClient;
    private final SlackMessageHelper slackHelper;

    public XbpChecksService(
        BlobRouterServiceClient blobClient,
        BankHolidayClient bankHolidayClient,
        SlackMessageHelper slackHelper
    ) {
        this.blobClient = blobClient;
        this.bankHolidayClient = bankHolidayClient;
        this.slackHelper = slackHelper;
    }

    /**
     * Entry point to run XBP checks.
     */
    public void runChecks() {
        List<String> actions = new ArrayList<>();

        checkXbpFiles(actions);

        sendSlackSummary(actions);
    }

    /**
     * Checks that XBP files have been processed today.
     * @param actions Record of any failures
     */
    private void checkXbpFiles(List<String> actions) {
        try {
            String today = LocalDate.now().toString();
            if (isBankHoliday(today)) {
                log.info("Today is a bank holiday, skipping XBP file check.");
                return;
            }
            ReportSummaryResponse xbpFiles = blobClient.getBlobReportsByDate(today);

            if (xbpFiles == null || xbpFiles.getTotalReceived() == 0) {
                actions.add("No files from XBP have come through for today.");
            }
        } catch (FeignException e) {
            log.error("Feign error while checking XBP files", e);
            actions.add("Failed to check XBP files due to network error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error while checking XBP files", e);
            actions.add("Failed to check XBP files. Check App insights.");
        }
    }

    private boolean isBankHoliday(String today) {
        try {
            BankHolidays holidays = bankHolidayClient.getBankHolidays();
            return holidays.englandAndWales.events.stream()
                .anyMatch(event -> today.equals(event.date));
        } catch (Exception e) {
            log.warn("Failed to check for bank holidays", e);
            return false;
        }
    }

    /**
     * Sends a summary of XBP checks to Slack.
     * @param actions Action descriptions to include in the summary
     */
    private void sendSlackSummary(List<String> actions) {
        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder sb = new StringBuilder(
            String.format("*:mag: XBP Daily Check (%s)*\n", timestamp)
        );
        if (actions.isEmpty()) {
            sb.append("> ✅ All clear! No XBP issues detected. :tada:");
        } else {
            sb.append("> ❗ XBP issue found:\n");
            actions.forEach(action -> sb.append("• ").append(action).append("\n"));
        }
        slackHelper.sendLongMessage(sb.toString());
    }
}
