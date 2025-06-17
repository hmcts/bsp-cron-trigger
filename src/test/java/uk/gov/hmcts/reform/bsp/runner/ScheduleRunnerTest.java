package uk.gov.hmcts.reform.bsp.runner;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.CronTimerProperties;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.triggers.BulkPrintChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

import java.util.List;
import java.util.stream.Stream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleRunnerTest {

    @InjectMocks
    ScheduleRunner scheduleRunner;

    @Mock
    private List<Trigger> mockArrayList;

    @Mock
    CronTimerProperties cronTimerProperties;

    @Mock
    BulkPrintChecksTrigger bulkPrintChecksTrigger;

    private static final String MESSAGE_DO_NOT_MATCH_MESSAGE = "Messages do not match";
    private static final String STATUS_DO_NOT_MATCH_MESSAGE = "Status codes do not match";

    @Test
    void testRunnerWhereInvalidArgProvided() throws Exception {
        try (LogCaptor logCaptor = LogCaptor.forClass(ScheduleRunner.class)) {

            when(cronTimerProperties.getTriggerType()).thenReturn("UNKNOWN_ENUM");
            when(cronTimerProperties.isEnabled()).thenReturn(true);

            int statusCode = catchSystemExit(() -> {
                scheduleRunner.run();
            });

            assertTrue(
                logCaptor.getErrorLogs().getFirst().contains("Invalid or no schedule type set. Exiting"),
                MESSAGE_DO_NOT_MATCH_MESSAGE
            );

            assertEquals(1, statusCode, STATUS_DO_NOT_MATCH_MESSAGE);
        }
    }

    @Test
    void testRunnerWhereSuccessfullyTriggered() {
        when(mockArrayList.stream()).thenReturn(Stream.of(bulkPrintChecksTrigger));
        when(bulkPrintChecksTrigger.isApplicable(ScheduleTypes.BULK_PRINT_CHECKS))
            .thenReturn(true);
        doNothing().when(bulkPrintChecksTrigger).trigger();

        when(cronTimerProperties.getTriggerType()).thenReturn("BULK_PRINT_CHECKS");
        when(cronTimerProperties.isEnabled()).thenReturn(true);

        scheduleRunner.run();
        verify(bulkPrintChecksTrigger, Mockito.times(1)).trigger();
    }

    @Test
    void testRunnerNotEnabled() throws Exception {
        try (LogCaptor logCaptor = LogCaptor.forClass(ScheduleRunner.class)) {

            when(cronTimerProperties.getTriggerType()).thenReturn("UNKNOWN_ENUM");
            when(cronTimerProperties.isEnabled()).thenReturn(false);

            int statusCode = catchSystemExit(() -> {
                scheduleRunner.run();
            });

            assertTrue(
                logCaptor.getErrorLogs().getFirst().contains("Trigger runner is disabled for UNKNOWN_ENUM."),
                MESSAGE_DO_NOT_MATCH_MESSAGE
            );

            assertEquals(1, statusCode, STATUS_DO_NOT_MATCH_MESSAGE);
        }
    }
}
