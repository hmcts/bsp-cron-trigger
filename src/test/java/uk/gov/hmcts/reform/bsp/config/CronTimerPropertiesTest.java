package uk.gov.hmcts.reform.bsp.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CronTimerPropertiesTest {

    private static final String TRIGGER_TYPE = "TRIGGER_TYPE";

    @Test
    void testCronTimerPropertiesSetsCorrectly() {

        CronTimerProperties cronTimerProperties = new CronTimerProperties();
        cronTimerProperties.setTriggerType(TRIGGER_TYPE);

        assertEquals(TRIGGER_TYPE, cronTimerProperties.getTriggerType(), "Unexpected trigger type returned");
    }

}
