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

    public void runDailyChecks() {
        List<String> actions = new ArrayList<>();

        handleBlobCleanup(actions);
        handleEnvelopeProcessing(actions);
        handlePaymentRetries(actions);

        log.debug("Daily checks completed, actions: {}", actions);
        sendSlackSummary(actions);
    }

    private void handleBlobCleanup(List<String> actions) {
        try {
            blobClient.deleteAllStaleBlobs(STALE_HOURS);
        } catch (Exception e) {
            actions.add("Failed to remove stale blobs: " + e.getMessage());
        }
    }

    private void handleEnvelopeProcessing(List<String> actions) {
        SearchResult<EnvelopeInfo> staleEnvs = fetchStaleEnvelopes();
        for (EnvelopeInfo info : staleEnvs.getData()) {
            String id = info.getEnvelopeId().toString();
            if (!tryDeleteEnvelope(id, actions)) {
                continue;
            }
            tryReprocessEnvelope(id, actions);
        }
    }

    private SearchResult<EnvelopeInfo> fetchStaleEnvelopes() {
        SearchResult<EnvelopeInfo> resp;
        try {
            resp = processorClient.getStaleIncompleteEnvelopes();
        } catch (Exception e) {
            log.error("Error fetching stale envelopes", e);
            slackHelper.sendLongMessage(
                "*:rotating_light: Could not fetch stale envelopes!*\n> " + e.getMessage()
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

    private boolean tryDeleteEnvelope(String id, List<String> actions) {
        try {
            processorClient.deleteStaleEnvelope(id, STALE_HOURS);
            return true;
        } catch (Exception e) {
            actions.add("Delete envelope " + id + " ➞ " + e.getMessage());
            return false;
        }
    }

    private void tryReprocessEnvelope(String id, List<String> actions) {
        try {
            processorClient.reprocessEnvelope(
                "Bearer " + authProps.getBearerToken(),
                UUID.fromString(id)
            );
        } catch (Exception e) {
            actions.add("Reprocess envelope " + id + " ➞ " + e.getMessage());
        }
    }

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
                "Failed to retry payments—please investigate manually: " + e.getMessage()
            );
        }
    }

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

    private void sendSlackSummary(List<String> actions) {
        StringBuilder sb = new StringBuilder("*:spiral_note_pad: Today's Bulk Scan Actions:*\n");
        if (actions.isEmpty()) {
            sb.append("> No actions; all looks good! :tada:");
        } else {
            actions.forEach(a -> sb.append("• ").append(a).append("\n"));
        }
        slackHelper.sendLongMessage(sb.toString());
    }
}
