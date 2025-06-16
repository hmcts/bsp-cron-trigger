package uk.gov.hmcts.reform.bsp.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.trigger-type=BULK_PRINT_CHECKS",
    "app.enabled=true"}
)
class SendLetterServiceClientTest {

    @Autowired
    private SendLetterServiceClient client;

    @MockitoBean
    private SlackClient slackClient;

    @DisplayName("Should retrieve stale letters from real service")
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
}
