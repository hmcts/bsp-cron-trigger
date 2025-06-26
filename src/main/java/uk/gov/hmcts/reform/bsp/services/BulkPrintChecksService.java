package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.Instant;
import java.time.ZoneOffset;
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

    public void runDailyChecks() {
        StaleLetterResponse resp = fetchStaleLettersOrAbort();

        List<String> actions = new ArrayList<>();
        Instant oneWeekAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);
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
                actions.add("Letter " + letter.getId() + " ➞ " + e.getMessage());
            }
        }

        StringBuilder sb = new StringBuilder("*:spiral_note_pad: Today's Bulk Print Actions:*\n");
        if (actions.isEmpty()) {
            sb.append("> No actions; all looks good! :tada:");
        } else {
            actions.forEach(a -> sb.append("• ").append(a).append("\n"));
        }
        slackHelper.sendLongMessage(sb.toString());
    }

    private StaleLetterResponse fetchStaleLettersOrAbort() {
        try {
            return letterClient.getStaleLetters();
        } catch (Exception e) {
            log.error("Error fetching stale letters", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not fetch stale letters!*\n> " + e.getMessage()
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }
}
