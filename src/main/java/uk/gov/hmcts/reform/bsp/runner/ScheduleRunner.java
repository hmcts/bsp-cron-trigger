package uk.gov.hmcts.reform.bsp.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.config.CronTimerProperties;
import uk.gov.hmcts.reform.bsp.models.ScheduleTypes;
import uk.gov.hmcts.reform.bsp.triggers.Trigger;

import java.util.List;
import java.util.Optional;

/**
 * This is the runner that is started when the job begins.
 * It selects the correct trigger based on the input, and then exits.
 */
@Service
@Slf4j
@SuppressWarnings("PMD.DoNotTerminateVM")
public class ScheduleRunner implements CommandLineRunner {

    private final CronTimerProperties cronTimerProperties;
    private final List<? extends Trigger> triggers;


    public ScheduleRunner(CronTimerProperties cronTimerProperties, List<? extends Trigger> triggers) {
        this.cronTimerProperties = cronTimerProperties;
        this.triggers = triggers;
    }

    @Override
    public void run(String... args) {
        if (!EnumUtils.isValidEnum(ScheduleTypes.class, cronTimerProperties.getTriggerType())) {
            log.error("Invalid or no schedule type set. Exiting");
            System.exit(1);
        }

        ScheduleTypes triggerType = ScheduleTypes.valueOf(cronTimerProperties.getTriggerType());

        Optional<? extends Trigger> foundTrigger =
            triggers.stream().filter(trigger -> trigger.isApplicable(triggerType)).findFirst();

        if (foundTrigger.isPresent()) {
            foundTrigger.get().trigger();
        } else {
            log.error("Failed to find trigger. Exiting");
            System.exit(1);
        }
    }

}
