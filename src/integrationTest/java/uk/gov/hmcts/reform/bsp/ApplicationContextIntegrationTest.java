package uk.gov.hmcts.reform.bsp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.bsp.runner.ScheduleRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.enabled=true",
    "app.trigger-type=BULK_SCAN_CHECKS"
})
class ApplicationContextIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private ScheduleRunner scheduleRunner;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
