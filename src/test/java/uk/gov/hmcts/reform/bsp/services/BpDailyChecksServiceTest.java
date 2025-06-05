package uk.gov.hmcts.reform.bsp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BpDailyChecksServiceTest {

    @Mock
    private SlackClient slackClient;

    @InjectMocks
    private BpDailyChecksService bpDailyChecksService;

    @Test
    void runDailyChecks_shouldSendMessageWithExpectedContent() {
        bpDailyChecksService.runDailyChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(1)).sendSlackMessage(captor.capture());

        String expected =
            "*:spiral_note_pad: Today's Bulk Print Actions:*\n"
                + "• Look at bulk print letter that cannot be aborted.\n"
                + "• Send stand-up summary\n";

        assertEquals(expected, captor.getValue());
    }

    @Test
    void runDailyChecks_whenSlackClientThrows_shouldPropagateException() {
        doThrow(new RuntimeException("Slack API failure"))
            .when(slackClient).sendSlackMessage(anyString());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> bpDailyChecksService.runDailyChecks()
        );
        assertEquals("Slack API failure", ex.getMessage());
    }

    @Test
    void runDailyChecks_withNullSlackClient_shouldThrowNullPointerException() {
        BsDailyChecksService serviceWithNull = new BsDailyChecksService(null);
        assertThrows(NullPointerException.class, serviceWithNull::runDailyChecks);
    }
}

