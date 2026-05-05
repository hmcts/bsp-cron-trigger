package uk.gov.hmcts.reform.bsp.triggers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BulkPrintProcessingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkPrintProcessingTriggerTest {

    @Mock
    private BulkPrintProcessingService processingService;

    @InjectMocks
    private BulkPrintProcessingTrigger trigger;

    @Test
    void trigger_shouldInvokeStartProcessingTasksOnce() {
        trigger.trigger();
        verify(processingService, times(1)).startProcessingTasks();
    }

    @Test
    void trigger_whenServiceThrows_shouldPropagateException() {
        doThrow(new IllegalStateException("Illegal"))
            .when(processingService).startProcessingTasks();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> trigger.trigger()
        );
        assertEquals("Illegal", ex.getMessage());
    }

    @Test
    void isApplicable_shouldReturnTrueForBspDailyProcessing() {
        assertTrue(trigger.isApplicable(ScheduleTypes.BULK_PRINT_PROCESSING));
    }

    @Test
    void isApplicable_shouldReturnFalseForOtherTypes() {
        for (ScheduleTypes type : ScheduleTypes.values()) {
            if (type != ScheduleTypes.BULK_PRINT_PROCESSING) {
                assertFalse(trigger.isApplicable(type),
                            () -> "Expected not applicable for " + type);
            }
        }
    }

    @Test
    void isApplicable_shouldReturnFalseForNull() {
        assertFalse(trigger.isApplicable(null));
    }
}
