package uk.gov.hmcts.reform.bsp.services;

import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.CronTimerProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.CheckPostedTaskResponse;
import uk.gov.hmcts.reform.bsp.models.PostedReportTaskResponse;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkPrintChecksService {

    private final AuthorisationProperties authProps;
    private final SendLetterServiceClient letterClient;
    private final SlackMessageHelper slackHelper;
    private final CronTimerProperties cronTimerProperties;

    /**
     * Runs the daily check process for bulk print letters.
     * <ol>
     *   <li>Retrieves the reports processed within the retrieval window:</li>
     *     <ul>
     *       <li>If no reports have been processed, add an info message.</li>
     *       <li>Otherwise, for each response:
     *         <ul>
     *           <li>If the report is marked as "processing failed", add an alert containing the message.</li>
     *           <li>Otherwise, add an info message detailing the report code and the number of posted letters.</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     *   <li>Trigger the check-posted task on the send-letter-service, then:
     *     <ul>
     *       <li>If the response show that 0 files have been move to NO_REPORT_ABORTED, add an info message.</li>
     *       <li>Otherwise, add an alert message.</li>
     *     </ul>
     *   </li>
     *   <li>Fetch the list of stale letters from the send-letter-service, then for each stale letter:
     *     <ul>
     *       <li>Generate an alert containing the id of the letter, indicating a need to investigate.</li>
     *     </ul>
     *   </li>
     *   <li>Collect all messages and alerts into a single, formatted message with a summary heading, then post
     *       that message to the configured Slack channel.</li>
     * </ol>>
     */
    public void runDailyChecks() {

        final List<String> messages = new ArrayList<>();
        boolean success = true;

        success &= fetchProcessedReports(messages);
        success &= runCheckPostedTask(messages);
        success &= runStaleLettersCheck(messages);

        sendSlackMessage(messages, success);
    }

    private boolean fetchProcessedReports(final List<String> messages) {
        boolean result = true;
        LocalDateTime after = LocalDateTime.now().minus(
            cronTimerProperties.getBulkPrintProcessing().getProcessedReportsRetrievalWindow()
        );
        List<PostedReportTaskResponse> mptResp = fetchProcessedReportsOrAbort(after);
        if (mptResp == null || mptResp.isEmpty()) {
            messages.add(" ℹ️ *Fetch Processed Reports*: Complete; no reports processed");
        } else {
            for (PostedReportTaskResponse report : mptResp) {
                String scope = report.isInternational() ? "international" : "domestic";
                if (report.isProcessingFailed()) {
                    result = false;
                    messages.add(String.format(
                        " ❗ *Fetch Processed Reports*: %s (%s) ERROR: %s",
                        report.getReportCode(),
                        scope,
                        report.getErrorMessage()
                    ));
                } else {
                    messages.add(String.format(
                        " ✅ *Fetch Processed Reports*: %s (%s) complete; %d letters marked as posted.",
                        report.getReportCode(),
                        scope,
                        report.getMarkedPostedCount()
                    ));
                }
            }
        }
        return result;
    }

    private boolean runCheckPostedTask(final List<String> messages) {
        boolean result = true;
        CheckPostedTaskResponse ctResp = runCheckPostedTaskOrAbort();
        if (ctResp.getMarkedNoReportAbortedCount() <= 0) {
            messages.add(" ✅ *Check Posted*: Complete; no letters were affected.");
        } else {
            result = false;
            messages.add(String.format(
                "❗ *Check Posted*: Complete; %d letters were marked as NO_REPORT_ABORTED.",
                ctResp.getMarkedNoReportAbortedCount()
            ));
        }
        return result;
    }

    private boolean runStaleLettersCheck(final List<String> messages) {
        boolean result = true;
        StaleLetterResponse slResp = fetchStaleLettersOrAbort();
        if (slResp.getCount() > 0) {
            result = false;
            // alert on slack because a report was received and these docs weren't referenced
            for (StaleLetter letter : slResp.getStaleLetters()) {
                messages.add(String.format(" ❗ *Check Stale*: Investigate stale letter: %s ", letter.getId()));
            }
        }
        return result;
    }

    private void sendSlackMessage(final List<String> messages, final boolean success) {
        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder sb = new StringBuilder(
            String.format("*:printer: Bulk Print Daily Check (%s)*%n%n", timestamp)
        );

        // if there are stale letters, or some letters have been set to NO_REPORT_ABORTED,
        // indicate this in the header of the slack message
        if (success) {
            sb.append("> :tada: *No print issues were detected* :tada:\n");
        } else {
            sb.append("> :rotatinglight2: *Print issues were detected* :rotatinglight2:\n");
        }

        messages.forEach(a -> sb.append("• ").append(a).append("\n"));
        slackHelper.sendLongMessage(sb.toString());
        log.info(sb.toString());
    }

    /**
     * Retrieves the reports that have been processed within the configured time window.
     *
     * @return response containing result of the task
     * @throws IllegalStateException if an error occurs during the task
     */
    private List<PostedReportTaskResponse> fetchProcessedReportsOrAbort(LocalDateTime since) {
        try {
            return letterClient.fetchProcessedReports("Bearer " + authProps.getBearerToken(), since);
        } catch (Exception e) {
            log.error("Error fetching processed reports", e);
            slackHelper.sendLongMessage(
                String.format("*:rotating_light: Could not fetch processed reports: %s *%n> ", e.getMessage())
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }

    /**
     * Runs the Check Posted Task on the send letter service.
     *
     * @return response containing result of the check
     * @throws IllegalStateException if an error occurs during the task
     */
    private CheckPostedTaskResponse runCheckPostedTaskOrAbort() {
        try {
            return letterClient.runCheckPosted("Bearer " + authProps.getBearerToken());
        } catch (Exception e) {
            log.error("Error running check posted task", e);
            slackHelper.sendLongMessage(
                String.format("*:rotating_light: Could not run check posted task: %s *%n> ", e.getMessage())
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
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
                String.format("*:rotating_light: Could not fetch stale letters: %s *%n> ", e.getMessage())
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }
}
