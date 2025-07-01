package uk.gov.hmcts.reform.bsp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanOrchestratorClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanProcessorClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.EnvelopeInfo;
import uk.gov.hmcts.reform.bsp.models.Payment;
import uk.gov.hmcts.reform.bsp.models.SearchResult;
import uk.gov.hmcts.reform.bsp.models.UpdatePayment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkScanChecksServiceTest {

    @Mock private AuthorisationProperties authProps;
    @Mock private BlobRouterServiceClient blobClient;
    @Mock private BulkScanProcessorClient processorClient;
    @Mock private BulkScanOrchestratorClient orchestratorClient;
    @Mock private SlackMessageHelper slackHelper;

    @InjectMocks private BulkScanChecksService service;

    @BeforeEach
    void setUp() {
        lenient().when(authProps.getBearerToken()).thenReturn("dummy-token");
    }

    @Test
    void runDailyChecks_allHappyPath_sendsNoActionsMessage() {
        SearchResult<String> emptyBlobs = new SearchResult<>();
        emptyBlobs.setData(Collections.emptyList());
        when(blobClient.deleteAllStaleBlobs(168)).thenReturn(emptyBlobs);

        SearchResult<EnvelopeInfo> emptyEnvs = new SearchResult<>();
        emptyEnvs.setData(Collections.emptyList());
        when(processorClient.getStaleIncompleteEnvelopes()).thenReturn(emptyEnvs);

        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(Collections.emptyList());
        when(orchestratorClient.getFailedNewPayments()).thenReturn(Collections.emptyList());

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());

        String expected =
            "*:spiral_note_pad: Today's Bulk Scan Actions:*\n"
                + "> No actions; all looks good! :tada:";
        assertEquals(expected, captor.getValue());
    }

    @Test
    void runDailyChecks_blobDeletionFails_reportsBlobError() {
        when(blobClient.deleteAllStaleBlobs(168))
            .thenThrow(new RuntimeException("oops blobs"));

        SearchResult<EnvelopeInfo> emptyEnvs = new SearchResult<>();
        emptyEnvs.setData(Collections.emptyList());
        when(processorClient.getStaleIncompleteEnvelopes()).thenReturn(emptyEnvs);
        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(Collections.emptyList());
        when(orchestratorClient.getFailedNewPayments()).thenReturn(Collections.emptyList());

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failed to remove stale blobs. Check App insights."));
    }

    @Test
    void runDailyChecks_envelopeDeleteFails_reportsDeleteError() {
        SearchResult<String> emptyBlobs = new SearchResult<>();
        emptyBlobs.setData(Collections.emptyList());
        when(blobClient.deleteAllStaleBlobs(168)).thenReturn(emptyBlobs);

        EnvelopeInfo info = new EnvelopeInfo();
        UUID id = UUID.randomUUID();
        info.setEnvelopeId(id);
        SearchResult<EnvelopeInfo> sr = new SearchResult<>();
        sr.setData(List.of(info));
        when(processorClient.getStaleIncompleteEnvelopes()).thenReturn(sr);

        when(processorClient.deleteStaleEnvelope(id.toString(), 168))
            .thenThrow(new RuntimeException("delete error"));

        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(Collections.emptyList());
        when(orchestratorClient.getFailedNewPayments()).thenReturn(Collections.emptyList());

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Delete envelope " + id + " failed."));
    }

    @Test
    void runDailyChecks_envelopeReprocessFails_reportsReprocessError() {
        SearchResult<String> emptyBlobs = new SearchResult<>();
        emptyBlobs.setData(Collections.emptyList());
        when(blobClient.deleteAllStaleBlobs(168)).thenReturn(emptyBlobs);

        EnvelopeInfo info = new EnvelopeInfo();
        UUID id = UUID.randomUUID();
        info.setEnvelopeId(id);
        SearchResult<EnvelopeInfo> sr = new SearchResult<>();
        sr.setData(List.of(info));
        when(processorClient.getStaleIncompleteEnvelopes()).thenReturn(sr);

        SearchResult<UUID> deleteRes = new SearchResult<>();
        deleteRes.setData(Collections.emptyList());
        when(processorClient.deleteStaleEnvelope(id.toString(), 168)).thenReturn(deleteRes);

        doThrow(new RuntimeException("reprocerr"))
            .when(processorClient).reprocessEnvelope("Bearer dummy-token", id);

        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(Collections.emptyList());
        when(orchestratorClient.getFailedNewPayments()).thenReturn(Collections.emptyList());

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Reprocess envelope " + id + " failed."));
    }

    @Test
    void runDailyChecks_paymentRetriesFail_reportsBothUpdateAndNew() {
        SearchResult<String> emptyBlobs = new SearchResult<>();
        emptyBlobs.setData(Collections.emptyList());
        when(blobClient.deleteAllStaleBlobs(168)).thenReturn(emptyBlobs);

        SearchResult<EnvelopeInfo> emptyEnvs = new SearchResult<>();
        emptyEnvs.setData(Collections.emptyList());
        when(processorClient.getStaleIncompleteEnvelopes()).thenReturn(emptyEnvs);

        UpdatePayment upd = new UpdatePayment();
        UUID updId = UUID.randomUUID();
        upd.setId(updId);
        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(List.of(upd));
        when(orchestratorClient.retryUpdatePayment(updId.toString()))
            .thenThrow(new RuntimeException("update payment fail"));

        Payment nw = new Payment();
        UUID newId = UUID.randomUUID();
        nw.setId(newId);
        when(orchestratorClient.getFailedNewPayments()).thenReturn(List.of(nw));
        when(orchestratorClient.retryNewPayment(newId.toString()))
            .thenThrow(new RuntimeException("new payment fail"));

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        String out = captor.getValue();

        assertTrue(out.contains("Retry update payment " + updId + " ➞ update payment fail"));
        assertTrue(out.contains("Retry new payment " + newId + " ➞ new payment fail"));
    }
}
