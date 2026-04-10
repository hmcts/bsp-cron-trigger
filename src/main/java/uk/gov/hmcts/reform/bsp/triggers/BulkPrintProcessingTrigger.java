package uk.gov.hmcts.reform.bsp.triggers;

import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.services.BulkPrintProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkPrintProcessingTrigger implements Trigger {

    private final BulkPrintProcessingService buildBulkPrintProcessingService;

    @Override
    public void trigger() {
        buildBulkPrintProcessingService.startProcessingTasks();
    }

    @Override
    public boolean isApplicable(ScheduleTypes scheduleTypes) {
        return ScheduleTypes.BULK_PRINT_PROCESSING.equals(scheduleTypes);
    }
}
