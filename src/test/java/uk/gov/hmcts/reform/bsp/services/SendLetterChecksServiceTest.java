package uk.gov.hmcts.reform.bsp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.MissingReportsResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendLetterChecksServiceTest {

    @Mock
    private SendLetterServiceClient sendLetterServiceClient;

    @Mock
    private SlackMessageHelper slackHelper;

    private SendLetterChecksService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new SendLetterChecksService(sendLetterServiceClient, slackHelper, objectMapper);
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
        List<MissingReportsResponse> missingReports = List.of(
            new MissingReportsResponse("ServiceA", true)
        );
        when(sendLetterServiceClient.runCheckReports(anyString(), anyString())).thenReturn(missingReports);

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        String actual = captor.getValue();
        assertTrue(actual.contains("Missing reports found:"));
        assertTrue(actual.contains("ServiceA"));
        assertTrue(actual.contains("isInternational=true"));
    }

    @Test
    void runDailyChecks_whenCheckFails_reportsSendLetterCheckError() {
        when(sendLetterServiceClient.runCheckReports(anyString(), anyString()))
            .thenThrow(new RuntimeException("send-letter-fail"));

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failed to check for missing reports. Check App insights."));
    }

    @Test
    void runDailyChecks_when404WithBody_reportsMissingReports() {
        String body = "[{\"serviceName\":\"ServiceA\",\"isInternational\":true}]";
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(notFound.contentUTF8()).thenReturn(body);

        when(sendLetterServiceClient.runCheckReports(anyString(), anyString())).thenThrow(notFound);

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        String actual = captor.getValue();
        assertTrue(
            actual.contains("Missing reports found:"),
            "Should contain 'Missing reports found:' but was: " + actual);
        assertTrue(actual.contains("ServiceA"), "Should contain 'ServiceA' but was: " + actual);
        assertTrue(
            actual.contains("isInternational=true"),
            "Should contain 'isInternational=true' but was: " + actual);
    }

    @Test
    void runDailyChecks_when404WithoutBody_reportsAllClear() {
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(notFound.contentUTF8()).thenReturn(null);

        when(sendLetterServiceClient.runCheckReports(anyString(), anyString())).thenThrow(notFound);

        service.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("All clear! No Send Letter Service issues detected"));
    }
}
