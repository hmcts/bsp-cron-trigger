package uk.gov.hmcts.reform.bsp.triggers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.DailyChecksService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DailyChecksTriggerTest {

    @Mock
    private DailyChecksService dailyChecksService;

    @InjectMocks
    private DailyChecksTrigger trigger;

    @Test
    void trigger_shouldInvokeRunDailyChecksOnce() {
        trigger.trigger();
        verify(dailyChecksService, times(1)).runDailyChecks();
    }

    @Test
    void trigger_whenServiceThrows_shouldPropagateException() {
        doThrow(new IllegalStateException("Illegal"))
            .when(dailyChecksService).runDailyChecks();

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> trigger.trigger()
        );
        assertEquals("Illegal", ex.getMessage());
    }

    @Test
    void isApplicable_shouldReturnTrueForBspDailyChecks() {
        assertTrue(trigger.isApplicable(ScheduleTypes.BSP_DAILY_CHECKS));
    }

    @Test
    void isApplicable_shouldReturnFalseForOtherTypes() {
        for (ScheduleTypes type : ScheduleTypes.values()) {
            if (type != ScheduleTypes.BSP_DAILY_CHECKS) {
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
