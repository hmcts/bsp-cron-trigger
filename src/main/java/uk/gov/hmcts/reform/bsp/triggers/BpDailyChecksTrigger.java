package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BulkPrintChecksService;

@Component
@Slf4j
public class BpDailyChecksTrigger implements Trigger {

    private final BulkPrintChecksService bulkPrintChecksService;

    public BpDailyChecksTrigger(BulkPrintChecksService bulkPrintChecksService) {
        this.bulkPrintChecksService = bulkPrintChecksService;
    }

    @Override
    public void trigger() {
        bulkPrintChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BULK_PRINT_CHECKS.equals(scheduleTypes);
    }
}
