package uk.gov.hmcts.reform.bsp.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bsp.triggers.BulkPrintChecksTrigger;
import uk.gov.hmcts.reform.bsp.triggers.BulkScanChecksTrigger;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleTypesTest {

    @Test
    @DisplayName("Should contain exactly the expected enum values")
    void enumValues_areAsExpected() {
        Set<String> names = Arrays.stream(ScheduleTypes.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

        assertThat(names)
            .containsExactlyInAnyOrder("BULK_SCAN_CHECKS", "BULK_PRINT_CHECKS");
    }

    @Test
    @DisplayName("Each ScheduleTypes constant should map to the correct Trigger class")
    void triggerClass_mappingIsCorrect() {
        assertThat(ScheduleTypes.BULK_SCAN_CHECKS.getTriggerClass())
            .isEqualTo(BulkScanChecksTrigger.class);

        assertThat(ScheduleTypes.BULK_PRINT_CHECKS.getTriggerClass())
            .isEqualTo(BulkPrintChecksTrigger.class);
    }
}
