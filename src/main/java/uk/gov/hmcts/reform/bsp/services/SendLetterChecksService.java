package uk.gov.hmcts.reform.bsp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.MissingReportsResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SendLetterChecksService {

    private final SendLetterServiceClient sendLetterServiceClient;
    private final SlackMessageHelper slackHelper;
    private final ObjectMapper objectMapper;

    public SendLetterChecksService(
        SendLetterServiceClient sendLetterServiceClient,
        SlackMessageHelper slackHelper,
        ObjectMapper objectMapper
    ) {
        this.sendLetterServiceClient = sendLetterServiceClient;
        this.slackHelper = slackHelper;
        this.objectMapper = objectMapper;
    }

    /**
     * Entry point to run send letter service checks.
     */
    public void runDailyChecks() {
        Optional<String> action = checkMissingReports();

        slackHelper.sendDailyCheckSummary("Send Letter Service", ":mag:", action);
    }

    /**
     * Checks that there are no missing reports in the last 7 days.
     * @return Record of missing reports, if any
     */
    private Optional<String> checkMissingReports() {
        List<MissingReportsResponse> missingReports;
        try {
            LocalDate today = LocalDate.now();
            missingReports = sendLetterServiceClient.runCheckReports(
                today.minusDays(7).toString(),
                today.toString()
            );
        } catch (FeignException.NotFound e) {
            String content = e.contentUTF8();
            try {
                missingReports = objectMapper.readValue(
                    content,
                    new TypeReference<List<MissingReportsResponse>>() {
                    }
                );
            } catch (Exception ex) {
                missingReports = Collections.emptyList();
            }
        } catch (Exception e) {
            return Optional.of("Failed to check for missing reports. Check App insights.");
        }

        if (missingReports != null && !missingReports.isEmpty()) {
            return Optional.of(
                "Missing reports found: " + missingReports + ". Check App insights for details.");
        }

        return Optional.empty();
    }

}
