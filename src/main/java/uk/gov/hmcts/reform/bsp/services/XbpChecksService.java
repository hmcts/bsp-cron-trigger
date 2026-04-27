package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.feign.BankHolidayClient;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;

import java.time.LocalDate;
import java.util.Optional;

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
    public void runDailyChecks() {
        Optional<String> action = checkXbpFiles();

        slackHelper.sendDailyCheckSummary("XBP", ":mag:", action);
    }

    /**
     * Checks that XBP files have been processed today.
     * @return Record of any failure
     */
    private Optional<String> checkXbpFiles() {
        try {
            String today = LocalDate.now().toString();
            if (isBankHoliday(today)) {
                log.info("Today is a bank holiday, skipping XBP file check.");
                return Optional.empty();
            }
            if (blobClient.getBlobReportsByDate(today).getTotalReceived() == 0) {
                return Optional.of("No files from XBP have come through for today.");
            }
        } catch (Exception e) {
            log.error("Error while checking XBP files", e);
            return Optional.of("Failed to check XBP files. Check App insights.");
        }
        return Optional.empty();
    }

    private boolean isBankHoliday(String today) {
        try {
            return bankHolidayClient.getBankHolidays()
                .englandAndWales.events.stream()
                .anyMatch(event -> today.equals(event.date));
        } catch (Exception e) {
            log.warn("Failed to check for bank holidays", e);
            return false;
        }
    }

}
