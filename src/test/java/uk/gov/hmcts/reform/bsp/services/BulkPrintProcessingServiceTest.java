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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BulkPrintProcessingServiceTest {

    @Mock
    private SlackMessageHelper slackHelper;

    @Mock
    private AuthorisationProperties authorisationProperties;

    @Mock
    private SendLetterServiceClient letterClient;


    @InjectMocks
    private BulkPrintProcessingService bulkPrintProcessingService;

    @Test
    void startProcessingTasks_shouldStartProcessingTasksWithoutError() {

        bulkPrintProcessingService.startProcessingTasks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertTrue(actual.contains("Bulk Print Report Processing Started"),
                   "expected header to mention Bulk Print Report Processing Started, was:\n" + actual);
    }

    @Test
    void startProcessingTasks_whenSlackClientThrows_shouldPropagateException() {
        doThrow(new RuntimeException("Slack API failure"))
            .when(slackHelper).sendLongMessage(anyString());
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> bulkPrintProcessingService.startProcessingTasks()
        );
        assertEquals("Slack API failure", ex.getMessage());
    }

    @Test
    void startProcessingTasks_withNullSlackClient_shouldThrowNullPointerException() {
        BulkPrintProcessingService serviceWithNull = new BulkPrintProcessingService(null, null, null);
        assertThrows(NullPointerException.class, serviceWithNull::startProcessingTasks);
    }

    @Test
    void startProcessingTasks_abortsAndDoesntSendMessageWhenGetStaleLettersCallFails() {
        doThrow(new RuntimeException()).when(letterClient).runProcessReports(anyString());
        assertThrows(IllegalStateException.class, () -> bulkPrintProcessingService.startProcessingTasks());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper, times(1)).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertThat(actual).contains("Could not run process reports task");
    }
}

