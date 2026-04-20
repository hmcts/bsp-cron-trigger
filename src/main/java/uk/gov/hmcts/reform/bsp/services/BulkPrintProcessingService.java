package uk.gov.hmcts.reform.bsp.services;

import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintProcessingService {

    private final AuthorisationProperties authProps;
    private final SendLetterServiceClient letterClient;
    private final SlackMessageHelper slackHelper;

    public void startProcessingTasks() {

        startProcessReportsTaskOrAbort();

        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String message = String.format("*:printer: Bulk Print Report Processing Started (%s)*%n%n", timestamp);

        slackHelper.sendLongMessage(message);
        log.info(message);
    }

    /**
     * Starts the Process Reports Task on the send letter service.
     *
     * @throws IllegalStateException if an error occurs during the task
     */
    private void startProcessReportsTaskOrAbort() {
        try {
            letterClient.runProcessReports("Bearer " + authProps.getBearerToken());
        } catch (Exception e) {
            log.error("Error running process reports task", e);
            slackHelper.sendLongMessage(
                String.format("*:rotating_light: Could not run process reports task: %s *%n> ",e.getMessage())
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }
}
