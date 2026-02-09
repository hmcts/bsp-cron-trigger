package uk.gov.hmcts.reform.bsp.services;

import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.CheckPostedTaskResponse;
import uk.gov.hmcts.reform.bsp.models.PostedReportTaskResponse;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        final List<String> messages = new ArrayList<>();
        final AtomicBoolean flagErrors = new AtomicBoolean(false);

        List<PostedReportTaskResponse> mptResp = runProcessReportsTaskOrAbort();
        if (mptResp == null || mptResp.isEmpty()) {
            messages.add(" ℹ️ *Process Reports*: Complete; no reports processed.");
        } else {
            for (PostedReportTaskResponse report : mptResp) {
                messages.add(String.format(
                    " ✅ *Process Reports*: %s complete; %d letters marked as posted.",
                    report.getServiceName(),
                    report.getMarkedPostedCount()
                ));
            }
        }

        CheckPostedTaskResponse ctResp = runCheckPostedTaskOrAbort();
        if (ctResp.getMarkedNoReportAbortedCount() <= 0) {
            messages.add(" ✅ *Check Posted*: Complete; no letters were affected.");
        } else {
            flagErrors.compareAndSet(false, true);
            messages.add(String.format(
                "❗ *Check Posted*: Complete; %d letters were marked as NO_REPORT_ABORTED.",
                ctResp.getMarkedNoReportAbortedCount()
            ));
        }

        StaleLetterResponse slResp = fetchStaleLettersOrAbort();
        if (slResp.getCount() > 0) {
            flagErrors.compareAndSet(false, true);
            // alert on slack because a report was received and these docs weren't referenced
            for (StaleLetter letter : slResp.getStaleLetters()) {
                messages.add(String.format("❗ *Check Stale*: Investigate stale letter: %s ", letter.getId()));
            }
        }

        ZonedDateTime nowUk = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String timestamp = nowUk.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder sb = new StringBuilder(
            String.format("*:printer: Bulk Print Daily Check (%s)*\n\n", timestamp)
        );

        // if there are stale letters, or some letters have been set to NO_REPORT_ABORTED,
        // indicate this in the header of the slack message
        if (flagErrors.get()) {
            sb.append(">❗ *Print issues were detected*:\n");
        } else {
            sb.append("> :tada: *No print issues were detected*:\n");
        }

        messages.forEach(a -> sb.append("• ").append(a).append("\n"));
        slackHelper.sendLongMessage(sb.toString());
    }

    private List<PostedReportTaskResponse> runProcessReportsTaskOrAbort() {
        try {
            return letterClient.runProcessReports("Bearer " + authProps.getBearerToken());
        } catch (Exception e) {
            log.error("Error running process reports task", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not run process reports task! *\n> "
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }

    private CheckPostedTaskResponse runCheckPostedTaskOrAbort() {
        try {
            return letterClient.runCheckPosted("Bearer " + authProps.getBearerToken());
        } catch (Exception e) {
            log.error("Error running check posted task", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not run check posted task! *\n> "
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
                "*:rotating_light: Could not fetch stale letters! *\n> "
            );
            throw new IllegalStateException("Aborting bulk‐print checks", e);
        }
    }
}
