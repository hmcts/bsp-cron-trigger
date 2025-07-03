package uk.gov.hmcts.reform.bsp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanOrchestratorClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanProcessorClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.EnvelopeInfo;
import uk.gov.hmcts.reform.bsp.models.SearchResult;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BulkScanChecksService {

    private static final int STALE_HOURS = 168;

    private final AuthorisationProperties authProps;
    private final BlobRouterServiceClient blobClient;
    private final BulkScanProcessorClient processorClient;
    private final BulkScanOrchestratorClient orchestratorClient;
    private final SlackMessageHelper slackHelper;

    public BulkScanChecksService(
        AuthorisationProperties authProps,
        BlobRouterServiceClient blobClient,
        BulkScanProcessorClient processorClient,
        BulkScanOrchestratorClient orchestratorClient,
        SlackMessageHelper slackHelper
    ) {
        this.authProps = authProps;
        this.blobClient = blobClient;
        this.processorClient = processorClient;
        this.orchestratorClient = orchestratorClient;
        this.slackHelper = slackHelper;
    }

    /**
     * Entry point to run all daily checks:
     * <ul>
     *   <li>Blob cleanup</li>
     *   <li>Envelope processing</li>
     *   <li>Payment retries</li>
     * </ul>
     * A summary of any actions or errors is sent to Slack.
     */
    public void runDailyChecks() {
        List<String> actions = new ArrayList<>();

        handleBlobCleanup(actions);
        handleEnvelopeProcessing(actions);
        handlePaymentRetries(actions);

        sendSlackSummary(actions);
    }

    /**
     * Deletes stale blobs older than STALE_HOURS.
     * @param actions list to record any failures
     */
    private void handleBlobCleanup(List<String> actions) {
        try {
            blobClient.deleteAllStaleBlobs(STALE_HOURS);
        } catch (Exception e) {
            log.error("Error while deleting stale blobs", e);
            actions.add("Failed to remove stale blobs. Check App insights.");
        }
    }

    /**
     * Retrieves and processes stale envelopes.
     * <ol>
     *   <li>Delete stale envelopes</li>
     *   <li>Reprocess envelopes that were successfully deleted</li>
     * </ol>
     * @param actions list to record any failures
     */
    private void handleEnvelopeProcessing(List<String> actions) {
        SearchResult<EnvelopeInfo> staleEnvs = fetchStaleEnvelopes();
        for (EnvelopeInfo info : staleEnvs.getData()) {
            String id = info.getEnvelopeId().toString();
            tryReprocessEnvelope(id, actions);
        }
    }

    /**
     * Fetches stale incomplete envelopes from the processor; sends a Slack alert on failure.
     * @return SearchResult containing EnvelopeInfo list, never null
     */
    private SearchResult<EnvelopeInfo> fetchStaleEnvelopes() {
        SearchResult<EnvelopeInfo> resp;
        try {
            resp = processorClient.getStaleIncompleteEnvelopes();
        } catch (Exception e) {
            log.error("Error fetching stale envelopes", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not fetch stale envelopes!*\n> "
            );
            resp = null;
        }

        // Cater for a null response from the client
        if (resp == null) {
            SearchResult<EnvelopeInfo> empty = new SearchResult<>();
            empty.setCount(0);
            empty.setData(Collections.emptyList());
            return empty;
        }
        return resp;
    }

    /**
     * Attempts to reprocess an envelope, recording failures.
     * @param id      envelope ID
     * @param actions list to record failures
     */
    private void tryReprocessEnvelope(String id, List<String> actions) {
        try {
            processorClient.reprocessEnvelope(
                "Bearer " + authProps.getBearerToken(),
                UUID.fromString(id)
            );
        } catch (Exception e) {
            log.error("Error reprocessing envelope {}: {}", id, e.getMessage());
            actions.add("Reprocess envelope " + id + " failed.");
        }
    }

    /**
     * Fetches failed payments and retries both update and new payments,
     * aggregating any errors.
     * @param actions list to record failures
     */
    private void handlePaymentRetries(List<String> actions) {
        try {
            actions.addAll(
                retryPayments(
                    orchestratorClient.getFailedUpdatePayments(),
                    p -> orchestratorClient.retryUpdatePayment(p.getId().toString()),
                    "update"
                )
            );
            actions.addAll(
                retryPayments(
                    orchestratorClient.getFailedNewPayments(),
                    p -> orchestratorClient.retryNewPayment(p.getId().toString()),
                    "new"
                )
            );
        } catch (Exception e) {
            log.error("Failed to retry payments", e);
            actions.add(
                "Failed to retry payments."
            );
        }
    }

    /**
     * Generic helper to retry a list of payments, capturing errors.
     * @param payments list of payment objects
     * @param retryFn  function to call to retry a payment
     * @param type     descriptive label for the payment type
     * @param <T>      payment type with getId() method
     * @return list of error messages for retries that failed
     */
    private <T> List<String> retryPayments(
        List<T> payments,
        java.util.function.Consumer<T> retryFn,
        String type
    ) {
        if (payments == null) {
            return Collections.singletonList("Failed to fetch " + type + " payments");
        }
        List<String> errs = new ArrayList<>();
        for (T p : payments) {
            try {
                retryFn.accept(p);
            } catch (Exception e) {
                String id = safeGetId(p);
                errs.add("Retry " + type + " payment " + id + " ➞ " + e.getMessage());
            }
        }
        return errs;
    }

    /**
     * Uses reflection to safely extract the ID from a payment object.
     * @param p payment object
     * @param <T> type of the payment object
     * @return string representation of the ID, or placeholder on failure
     */
    private <T> String safeGetId(T p) {
        try {
            Object idObj = p.getClass().getMethod("getId").invoke(p);
            return idObj != null ? idObj.toString() : "<null>";
        } catch (NoSuchMethodException
                 | InvocationTargetException
                 | IllegalAccessException ex) {
            log.warn("Could not extract id via reflection", ex);
            return "<unknown>";
        }
    }

    /**
     * Sends a summary of today's actions (or lack thereof) to Slack.
     * @param actions list of action descriptions
     */
    private void sendSlackSummary(List<String> actions) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        StringBuilder sb = new StringBuilder(
            String.format("*:mag: Bulk Scan Daily Check (%s)*\n", timestamp)
        );
        if (actions.isEmpty()) {
            sb.append("> ✅ All clear! No scan issues detected. :tada:");
        } else {
            sb.append("> ❗ Scan issues found:\n");
            actions.forEach(a -> sb.append("• ").append(a).append("\n"));
        }
        slackHelper.sendLongMessage(sb.toString());
    }
}
