package uk.gov.hmcts.reform.bsp.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;
import uk.gov.hmcts.reform.bsp.models.CheckPostedTaskResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(properties = {
    "app.trigger-type=BULK_PRINT_CHECKS",
    "app.enabled=true",
    "app.bulk-print-processing.processed-reports-retrieval-window=45m"}
)
class SendLetterServiceClientTest {

    @Autowired
    private SendLetterServiceClient client;

    @Autowired
    private AuthorisationProperties authProps;

    @MockitoBean
    private SlackClient slackClient;

    @DisplayName("Should retrieve stale letters from bulk print")
    @Test
    void getStaleLettersTest() {
        StaleLetterResponse resp = client.getStaleLetters();

        assertThat(resp).isNotNull();
        assertThat(resp.getStaleLetters()).isNotNull();
        assertThat(resp.getCount())
            .withFailMessage("count field must equal number of staleLetters")
            .isEqualTo(resp.getStaleLetters().size());

        resp.getStaleLetters().forEach(letter -> {
            assertThat(letter.getId()).isNotNull();
            assertThat(letter.getStatus())
                .withFailMessage("status must not be blank")
                .isNotBlank();
            assertThat(letter.getService())
                .withFailMessage("service must not be blank")
                .isNotBlank();
            assertThat(letter.getCreatedAt())
                .withFailMessage("createdAt must not be in the future")
                .isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(letter.getSentToPrintAt())
                .withFailMessage("sentToPrintAt must be after or equal to createdAt")
                .isAfterOrEqualTo(letter.getCreatedAt());
        });
    }

    @DisplayName("Should process reports and return valid responses")
    @Test
    void runProcessReportsTest() {
        try {
            String bearerToken = "Bearer " + authProps.getBearerToken();
            client.runProcessReports(bearerToken);
        } catch (Exception e) {
            fail("Expected to be able to call the process reports endpoint: " + e.getMessage());
        }
    }

    @DisplayName("Should retrieve reports and return valid responses")
    @Test
    void fetchProcessedReportsTest() {
        try {
            String bearerToken = "Bearer " + authProps.getBearerToken();
            client.fetchProcessedReports(bearerToken, LocalDateTime.now());
        } catch (Exception e) {
            fail("Expected to be able to call the process reports endpoint: " + e.getMessage());
        }
    }

    @DisplayName("Should check posted and return valid response")
    @Test
    void runCheckPostedTest() {
        String bearerToken = "Bearer " + authProps.getBearerToken();
        CheckPostedTaskResponse resp = client.runCheckPosted(bearerToken);
        assertThat(resp).isNotNull();
        assertThat(resp.getMarkedNoReportAbortedCount()).isGreaterThanOrEqualTo(0);
    }
}
