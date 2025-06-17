package uk.gov.hmcts.reform.bsp.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.bsp.config.feign.BulkScanOrchestratorClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;
import uk.gov.hmcts.reform.bsp.models.Payment;
import uk.gov.hmcts.reform.bsp.models.UpdatePayment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.enabled=true",
    "app.trigger-type=BULK_SCAN_CHECKS"
})
class BulkScanOrchestratorClientTest {

    @Autowired
    private BulkScanOrchestratorClient client;

    @MockitoBean
    private SlackClient slackClient;

    @DisplayName("Should retrieve list of failed-new payments and deserialize correctly")
    @Test
    void getFailedNewPaymentsTest() {
        List<Payment> payments = client.getFailedNewPayments();

        assertThat(payments).isNotNull();

        payments.forEach(p -> {
            UUID id = p.getId();
            assertThat(id).withFailMessage("id must not be null").isNotNull();

            assertThat(p.getEnvelopeId())
                .withFailMessage("envelopeId must not be blank")
                .isNotBlank();
            assertThat(p.getCcdReference())
                .withFailMessage("ccdReference must not be blank")
                .isNotBlank();

            assertThat(p.getPoBox())
                .withFailMessage("poBox must not be blank")
                .isNotBlank();
            assertThat(p.getJurisdiction())
                .withFailMessage("jurisdiction must not be blank")
                .isNotBlank();
            assertThat(p.getService())
                .withFailMessage("service must not be blank")
                .isNotBlank();
            assertThat(p.getStatusMessage())
                .withFailMessage("statusMessage must not be blank")
                .isNotBlank();

            assertThat(p.getPayments())
                .withFailMessage("payments list must not be null")
                .isNotNull();

            assertThat(p.getStatus())
                .withFailMessage("status must not be null")
                .isNotNull();

            LocalDateTime created = p.getCreatedAt();
            LocalDateTime updated = p.getLastUpdatedAt();
            assertThat(created)
                .withFailMessage("createdAt must not be in the future")
                .isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(updated)
                .withFailMessage("lastUpdatedAt must be after or equal to createdAt")
                .isAfterOrEqualTo(created);
        });
    }

    @DisplayName("Should retrieve list of failed-update payments and deserialize correctly")
    @Test
    void getFailedUpdatePaymentsTest() {
        List<UpdatePayment> updates = client.getFailedUpdatePayments();

        assertThat(updates).isNotNull();

        updates.forEach(u -> {
            assertThat(u.getId())
                .withFailMessage("id must not be null")
                .isNotNull();
            assertThat(u.getEnvelopeId())
                .withFailMessage("envelopeId must not be blank")
                .isNotBlank();
            assertThat(u.getJurisdiction())
                .withFailMessage("jurisdiction must not be blank")
                .isNotBlank();
            assertThat(u.getExceptionRecordRef())
                .withFailMessage("exceptionRecordRef must not be blank")
                .isNotBlank();
            assertThat(u.getNewCaseRef())
                .withFailMessage("newCaseRef must not be blank")
                .isNotBlank();
            assertThat(u.getStatusMessage())
                .withFailMessage("statusMessage must not be blank")
                .isNotBlank();
            assertThat(u.getStatus())
                .withFailMessage("status must not be null")
                .isNotNull();

            LocalDateTime created = u.getCreatedAt();
            LocalDateTime updated = u.getLastUpdatedAt();
            assertThat(created)
                .withFailMessage("createdAt must not be in the future")
                .isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(updated)
                .withFailMessage("lastUpdatedAt must be after or equal to createdAt")
                .isAfterOrEqualTo(created);
        });
    }
}
