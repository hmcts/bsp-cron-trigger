package uk.gov.hmcts.reform.bsp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.CheckPostedTaskResponse;
import uk.gov.hmcts.reform.bsp.models.PostedReportTaskResponse;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkPrintChecksServiceTest {

    @Mock
    private SlackMessageHelper slackHelper;

    @Mock
    private AuthorisationProperties authorisationProperties;

    @Mock
    private SendLetterServiceClient letterClient;

    @InjectMocks
    private BulkPrintChecksService bulkPrintChecksService;

    @Test
    void runDailyChecks_shouldSendMessageWithExpectedStaleLetterToCheck() {
        when(letterClient.getStaleLetters()).thenReturn(
            new StaleLetterResponse(
                1, List.of(new StaleLetter(
                UUID.fromString("c89ad43b-5079-47b2-9984-8a122e115d06"),
                "mosh",
                "kupo",
                LocalDateTime.of(2000, 8, 20, 20, 20, 20),
                LocalDateTime.of(2001, 8, 20, 20, 20, 20)
            ), new StaleLetter(
                UUID.fromString("c89ad43b-5079-47b2-9984-8a122e115d07"),
                "mosh 2",
                "kupo 2",
                LocalDateTime.of(2000, 9, 21, 21, 21, 21),
                LocalDateTime.of(2001, 9, 21, 21, 21, 21))
            )));
        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(0));

        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertTrue(actual.contains("Bulk Print"),
                   "expected header to mention Bulk Print, was:\n" + actual);
        assertTrue(actual.contains("❗ *Check Stale*: Investigate stale letter: c89ad43b-5079-47b2-9984-8a122e115d06"),
                   "missing first investigate line");
        assertTrue(actual.contains("❗ *Check Stale*: Investigate stale letter: c89ad43b-5079-47b2-9984-8a122e115d07"),
                   "missing second investigate line");
    }

    @Test
    void runDailyChecks_shouldSendMessageWithNoStaleLettersToCheck() {
        when(letterClient.getStaleLetters()).thenReturn(
            new StaleLetterResponse(
                0, Collections.emptyList()
            ));
        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(0));
        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();

        assertAll("bulk-print no-issues summary",
                  () -> assertTrue(actual.contains("Bulk Print Daily Check")),
                  () -> assertTrue(actual.contains(":tada: *No print issues were detected*"))
        );
    }

    @Test
    void runDailyChecks_whenSlackClientThrows_shouldPropagateException() {
        when(letterClient.getStaleLetters()).thenReturn(
            new StaleLetterResponse(
                1, List.of(new StaleLetter(
                UUID.fromString("c89ad43b-5079-47b2-9984-8a122e115d08"),
                "mosh 3",
                "kupo 4",
                LocalDateTime.of(2001, 8, 20, 20, 20, 20),
                LocalDateTime.of(2002, 8, 20, 20, 20, 20)
            ))
            ));
        doThrow(new RuntimeException("Slack API failure"))
            .when(slackHelper).sendLongMessage(anyString());
        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(0));
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> bulkPrintChecksService.runDailyChecks()
        );
        assertEquals("Slack API failure", ex.getMessage());
    }

    @Test
    void runDailyChecks_withNullSlackClient_shouldThrowNullPointerException() {
        BulkScanChecksService serviceWithNull = new BulkScanChecksService(null, null, null, null, null);
        assertThrows(NullPointerException.class, serviceWithNull::runDailyChecks);
    }

    @Test
    void runDailyChecks_shouldSendMessageWithProcessReportsDetails() {
        LocalDate now = LocalDate.now();
        PostedReportTaskResponse prtrA = new PostedReportTaskResponse("SSCS-IB", now, false);
        prtrA.setMarkedPostedCount(100);
        PostedReportTaskResponse prtrB = new PostedReportTaskResponse("SSCS-IB", now, false);
        prtrB.setProcessingFailed(true);
        prtrB.setErrorMessage("error message");
        PostedReportTaskResponse prtrC = new PostedReportTaskResponse("SSCS-REFORM", now, false);
        prtrA.setMarkedPostedCount(100);
        PostedReportTaskResponse prtrD = new PostedReportTaskResponse("NFDIV", now, true);
        prtrA.setMarkedPostedCount(100);

        when(letterClient.runProcessReports(anyString())).thenReturn(
            List.of(prtrA, prtrB, prtrC, prtrD)
        );

        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(0));
        when(letterClient.getStaleLetters()).thenReturn(new StaleLetterResponse(0, Collections.emptyList()));

        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains(
            "Bulk Print",
            "SSCS-IB (domestic) complete; 100 letters marked as posted",
            "SSCS-IB (domestic) ERROR: error message",
            "SSCS-REFORM (domestic) complete; 0 letters marked as posted",
            "NFDIV (international) complete; 0 letters marked as posted"
        );
    }

    @Test
    void runDailyChecks_shouldSendMessageWithCheckPostedDetails() {
        when(letterClient.runProcessReports(anyString())).thenReturn(Collections.emptyList());
        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(30));
        when(letterClient.getStaleLetters()).thenReturn(new StaleLetterResponse(0, Collections.emptyList()));

        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains(
            "Bulk Print",
            "30 letters were marked as NO_REPORT_ABORTED."
        );
    }

    @Test
    void runDailyChecks_abortsAndDoesntSendMessageWhenProcessReportsCallFails() {
        when(letterClient.runProcessReports(anyString())).thenThrow(new RuntimeException());

        assertThrows(IllegalStateException.class, () -> bulkPrintChecksService.runDailyChecks());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains("Could not run process reports task");
    }

    @Test
    void runDailyChecks_abortsAndDoesntSendMessageWhenCheckPostedCallFails() {
        when(letterClient.runProcessReports(anyString())).thenReturn(Collections.emptyList());
        when(letterClient.runCheckPosted(any(String.class))).thenThrow(new RuntimeException());

        assertThrows(IllegalStateException.class, () -> bulkPrintChecksService.runDailyChecks());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains("Could not run check posted task");
    }

    @Test
    void runDailyChecks_abortsAndDoesntSendMessageWhenGetStaleLettersCallFails() {
        when(letterClient.runProcessReports(anyString())).thenReturn(Collections.emptyList());
        when(letterClient.runCheckPosted(any(String.class))).thenReturn(new CheckPostedTaskResponse(30));
        when(letterClient.getStaleLetters()).thenThrow(new RuntimeException());

        assertThrows(IllegalStateException.class, () -> bulkPrintChecksService.runDailyChecks());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains("Could not fetch stale letters");
    }
}

