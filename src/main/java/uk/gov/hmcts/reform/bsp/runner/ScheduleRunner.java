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
        if (!cronTimerProperties.isEnabled()) {
            log.warn("Trigger runner is disabled for {}.", cronTimerProperties.getTriggerType());
            System.exit(1);
        }

        Optional.ofNullable(EnumUtils.getEnum(
            ScheduleTypes.class,
            cronTimerProperties.getTriggerType()
        )).flatMap(type ->
                       triggers.stream()
                           .filter(t -> t.isApplicable(type))
                           .findFirst()
        ).ifPresentOrElse(
            Trigger::trigger,
            () -> {
                log.error("Invalid or no schedule type set. Exiting");
                System.exit(1);
            }
        );
    }
}
