package uk.gov.hmcts.reform.bsp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.feign.BankHolidayClient;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendLetterChecksServiceTest {

    @Mock
    private SendLetterServiceClient sendLetterServiceClient;

    @Mock
    private SlackMessageHelper slackHelper;

    @InjectMocks
    private SendLetterChecksService service;

    @BeforeEach
    void setUp() {

    }

    @Test
    void runDailyChecks_whenMissingReportsNotFound_reportsSuccess() {
        List<MissingReportsResponse> missingReports = Collections.emptyList();
        when(sendLetterServiceClient.runCheckReports(anyString(), anyString())).thenReturn(missingReports);

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertAll(
            "Send Letter Service no-issues summary",
            () -> assertTrue(actual.contains("Send Letter Service Daily Check")),
            () -> assertTrue(actual.contains("All clear! No Send Letter Service issues detected"))
        );
    }

    @Test
    void runDailyChecks_whenMissingReportsFound_reportsSendLetterError() {
        List<MissingReportsResponse> missingReports = List.of(new MissingReportsResponse[]{
            new MissingReportsResponse("ServiceA", true),
        });
        when(sendLetterServiceClient.runCheckReports(anyString(), anyString())).thenReturn(missingReports);

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains(
            "Missing reports found: [MissingReportsResponse(service=ServiceA, missing=true)]." +
                "Check App insights for details."));
    }

    @Test
    void runDailyChecks_whenCheckFails_reportsXbpCheckError() {
        when(sendLetterServiceClient.runCheckReports(anyString(), anyString()))
            .thenThrow(new RuntimeException("send-letter-fail"));

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failed to check for missing reports. Check App insights."));
    }
}
