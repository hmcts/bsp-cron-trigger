package uk.gov.hmcts.reform.bsp.triggers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BulkScanChecksService;

@Component
@Slf4j
public class BulkScanChecksTrigger implements Trigger {
    private final BulkScanChecksService bulkScanChecksService;

    public BulkScanChecksTrigger(BulkScanChecksService bulkScanChecksService) {
        this.bulkScanChecksService = bulkScanChecksService;
    }

    @Override
    public void trigger() {
        bulkScanChecksService.runDailyChecks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BULK_SCAN_CHECKS.equals(scheduleTypes);
    }
}
