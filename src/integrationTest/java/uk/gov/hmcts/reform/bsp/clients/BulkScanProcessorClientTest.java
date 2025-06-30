package uk.gov.hmcts.reform.bsp.clients;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanOrchestratorClient;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanProcessorClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.EnvelopeInfo;
import uk.gov.hmcts.reform.bsp.models.EnvelopeResponse;
import uk.gov.hmcts.reform.bsp.models.SearchResult;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "app.enabled=true",
    "app.trigger-type=BULK_SCAN_CHECKS"
})
class BulkScanProcessorClientTest {

    @Autowired
    private BulkScanProcessorClient client;

    @MockitoBean
    private AuthorisationProperties authorisationProperties;
    @MockitoBean
    private SlackMessageHelper slackMessageHelper;
    @MockitoBean
    private BlobRouterServiceClient blobRouterServiceClient;
    @MockitoBean
    private BulkScanOrchestratorClient orchestratorClient;

    @BeforeEach
    void safeSchedulerStubs() {
        SearchResult<EnvelopeInfo> emptyEnvs = new SearchResult<>();
        emptyEnvs.setData(Collections.emptyList());
        when(orchestratorClient.getFailedUpdatePayments()).thenReturn(Collections.emptyList());
        when(orchestratorClient.getFailedNewPayments()).thenReturn(Collections.emptyList());
    }

    @DisplayName("Should retrieve stale incomplete envelopes and deserialize correctly")
    @Test
    void getStaleIncompleteEnvelopesTest() {
        SearchResult<EnvelopeInfo> result = client.getStaleIncompleteEnvelopes();

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();

        assertThat(result.getCount())
            .withFailMessage("count must equal number of results")
            .isEqualTo(result.getData().size());

        result.getData().forEach(env -> {
            assertThat(env.getContainer())
                .withFailMessage("container must not be blank")
                .isNotBlank();
            assertThat(env.getFileName())
                .withFailMessage("fileName must not be blank")
                .isNotBlank();
            assertThat(env.getEnvelopeId())
                .withFailMessage("envelopeId must not be null")
                .isNotNull();
            assertThat(env.getCreatedAt())
                .withFailMessage("createdAt must not be in the future")
                .isBeforeOrEqualTo(Instant.now());
        });
    }

    @DisplayName("Should fetch envelope details and deserialize correctly")
    @Test
    void fetchEnvelopeDetailsTest() {
        SearchResult<EnvelopeInfo> result = client.getStaleIncompleteEnvelopes();
        Assumptions.assumeTrue(
            !result.getData().isEmpty(),
            "No stale envelopes available to fetch details for,"
                + " skipping validations"
        );

        EnvelopeInfo env = result.getData().getFirst();
        EnvelopeResponse resp = client.fetchEnvelopeDetails(env.getContainer(), env.getFileName());

        assertThat(resp).isNotNull();
        assertThat(resp.getId())
            .withFailMessage("response.id must not be null")
            .isNotNull();
        assertThat(resp.getCaseNumber())
            .withFailMessage("caseNumber must not be blank")
            .isNotBlank();
        assertThat(resp.getContainer())
            .withFailMessage("container must not be blank")
            .isNotBlank();
        assertThat(resp.getPoBox())
            .withFailMessage("poBox must not be blank")
            .isNotBlank();
        assertThat(resp.getJurisdiction())
            .withFailMessage("jurisdiction must not be blank")
            .isNotBlank();
        assertThat(resp.getDeliveryDate())
            .withFailMessage("deliveryDate must not be in the future")
            .isBeforeOrEqualTo(Instant.now());
        assertThat(resp.getOpeningDate())
            .withFailMessage("openingDate must not be in the future")
            .isBeforeOrEqualTo(Instant.now());
        assertThat(resp.getZipFileCreatedDate())
            .withFailMessage("zipFileCreatedDate must not be in the future")
            .isBeforeOrEqualTo(Instant.now());
        assertThat(resp.getZipFileName())
            .withFailMessage("zipFileName must not be blank")
            .isNotBlank();

        assertThat(resp.getStatus())
            .withFailMessage("status must not be null")
            .isNotNull();
        assertThat(resp.getClassification())
            .withFailMessage("classification must not be blank")
            .isNotBlank();

        assertThat(resp.getScannableItems())
            .withFailMessage("scannableItems must not be null")
            .isNotNull();
        assertThat(resp.getPayments())
            .withFailMessage("payments must not be null")
            .isNotNull();
        assertThat(resp.getNonScannableItems())
            .withFailMessage("nonScannableItems must not be null")
            .isNotNull();
    }
}
