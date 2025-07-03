package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BulkPrintChecksService {

    private final AuthorisationProperties authProps;
    private final SendLetterServiceClient letterClient;
    private final SlackMessageHelper slackHelper;

    public BulkPrintChecksService(
        AuthorisationProperties authProps,
        SendLetterServiceClient letterClient,
        SlackMessageHelper slackHelper
    ) {
        this.authProps = authProps;
        this.letterClient = letterClient;
        this.slackHelper = slackHelper;
    }

    /**
     * Runs the daily check process for bulk print letters.
     * <ol>
     *   <li>Fetch the list of stale letters (older than a set threshold).</li>
     *   <li>For each letter:
     *       <ul>
     *         <li>If the letter is older than one week or still in "Uploaded" status, mark it as "Aborted".</li>
     *         <li>Otherwise, mark it as "Created".</li>
     *       </ul>
     *   </li>
     *   <li>Collect any failures to notify for investigation.</li>
     *   <li>Send a Slack message summarising today's actions.</li>
     * </ol>
     */
    public void runDailyChecks() {
        StaleLetterResponse resp = fetchStaleLettersOrAbort();

        List<String> actions = new ArrayList<>();
        Instant oneWeekAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60L);
        for (StaleLetter letter : resp.getStaleLetters()) {
            Instant created = letter.getCreatedAt().toInstant(ZoneOffset.UTC);
            try {
                if (created.isBefore(oneWeekAgo) || "Uploaded".equals(letter.getStatus())) {
                    letterClient.markAborted(
                        letter.getId().toString(),
                        "Bearer " + authProps.getBearerToken()
                    );
                } else {
                    letterClient.markCreated(
                        letter.getId().toString(),
                        "Bearer " + authProps.getBearerToken()
                    );
                }
            } catch (Exception e) {
                log.warn("Exception occurred while marking aborted or created {}", letter.getId(), e);
                actions.add("Investigate Letter " + letter.getId());
            }
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder sb = new StringBuilder(
            String.format("*:printer: Bulk Print Daily Check (%s)*\n", timestamp)
        );
        if (actions.isEmpty()) {
            sb.append("> ✅ All clear! No print issues detected. :tada:");
        } else {
            sb.append("> ❗ Print issues found:\n");
            actions.forEach(a -> sb.append("• ").append(a).append("\n"));
        }
        slackHelper.sendLongMessage(sb.toString());
    }

    /**
     * Fetches stale letters from the SendLetterService, sending an alert
     * to Slack and aborting the process if the fetch fails.
     *
     * @return response containing the list of stale letters
     * @throws IllegalStateException if unable to fetch letters
     */
    private StaleLetterResponse fetchStaleLettersOrAbort() {
        try {
            return letterClient.getStaleLetters();
        } catch (Exception e) {
            log.error("Error fetching stale letters", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not fetch stale letters! *\n> "
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }
}
