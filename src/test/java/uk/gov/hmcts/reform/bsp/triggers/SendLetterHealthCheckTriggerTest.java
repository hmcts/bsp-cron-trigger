package uk.gov.hmcts.reform.bsp.triggers;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bsp.config.feign.SendLetterServiceClient;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendLetterHealthCheckTriggerTest {

    private SendLetterServiceClient sendLetterServiceClient;
    private SendLetterHealthCheckTrigger trigger;
    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        sendLetterServiceClient = mock(SendLetterServiceClient.class);
        trigger = new SendLetterHealthCheckTrigger(sendLetterServiceClient);

        logCaptor = LogCaptor.forClass(SendLetterHealthCheckTrigger.class);
        logCaptor.clearLogs();
    }

    @Test
    void shouldLogInfoWhenHealthIsUp() {
        when(sendLetterServiceClient.getHealth()).thenReturn("UP");
        trigger.trigger();

        assertThat(logCaptor.getInfoLogs()).contains("Send letter health check passing");
    }

    @Test
    void shouldLogErrorWhenHealthIsNotUp() {
        when(sendLetterServiceClient.getHealth()).thenReturn("DOWN");
        trigger.trigger();

        assertThat(logCaptor.getErrorLogs()).contains("Send letter health check failed");
    }

    @Test
    void shouldBeApplicableForSendLetterHealthCheck() {
        boolean result = trigger.isApplicable(ScheduleTypes.SEND_LETTER_HEALTH_CHECK);

        assertThat(result).isTrue();
    }
}
