package uk.gov.hmcts.reform.bsp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.AuthorisationProperties;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.StaleLetter;
import uk.gov.hmcts.reform.bsp.models.StaleLetterResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        doThrow(RuntimeException.class).when(letterClient).markAborted(anyString(), anyString());

        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String expected =
            "*:spiral_note_pad: Today's Bulk Print Actions:*\n"
            + "• Investigate Letter c89ad43b-5079-47b2-9984-8a122e115d06\n"
            + "• Investigate Letter c89ad43b-5079-47b2-9984-8a122e115d07\n";

        assertEquals(expected, captor.getValue());
    }

    @Test
    void runDailyChecks_shouldSendMessageWithNoStaleLettersToCheck() {
        when(letterClient.getStaleLetters()).thenReturn(
            new StaleLetterResponse(
                1, List.of(new StaleLetter(
                UUID.fromString("c89ad43b-5079-47b2-9984-8a122e115d06"),
                "mosh",
                "kupo",
                LocalDateTime.of(2000, 8, 20, 20, 20, 20),
                LocalDateTime.of(2001, 8, 20, 20, 20, 20)
            ))
            ));

        bulkPrintChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String expected =
            "*:spiral_note_pad: Today's Bulk Print Actions:*\n"
                + "> No actions; all looks good! :tada:";

        assertEquals(expected, captor.getValue());
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
}

