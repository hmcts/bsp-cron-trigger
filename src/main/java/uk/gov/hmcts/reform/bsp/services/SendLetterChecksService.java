package uk.gov.hmcts.reform.bsp.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        this.objectMapper = objectMapper.copy().registerModule(new JavaTimeModule());
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
        try {
            List<MissingReportsResponse> missingReports = getMissingReports();

            if (missingReports != null && !missingReports.isEmpty()) {
                return Optional.of(formatMissingReportsMessage(missingReports));
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.of("Failed to check for missing reports. Check App insights.");
        }
    }

    private List<MissingReportsResponse> getMissingReports() {
        try {
            LocalDate today = LocalDate.now();
            return sendLetterServiceClient.runCheckReports(
                today.minusDays(7).toString(),
                today.minusDays(1).toString()
            );
        } catch (FeignException.NotFound e) {
            return parseMissingReportsResponse(e.contentUTF8());
        }
    }

    private List<MissingReportsResponse> parseMissingReportsResponse(String content) {
        try {
            if (content == null || content.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(
                content,
                new TypeReference<List<MissingReportsResponse>>() {
                }
            );
        } catch (Exception ex) {
            log.error("Failed to parse missing reports response: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String formatMissingReportsMessage(List<MissingReportsResponse> missingReports) {
        StringBuilder sb = new StringBuilder("Missing reports found: [");
        for (int i = 0; i < missingReports.size(); i++) {
            MissingReportsResponse report = missingReports.get(i);
            sb.append(String.format(
                "MissingReportsResponse(serviceName=%s, isInternational=%b, date=%s)",
                report.getServiceName(),
                report.isInternational(),
                report.getReportDate()
            ));
            if (i < missingReports.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]. Check App insights for details.");
        return sb.toString();
    }

}
