package com.ttgint.scheduler.utils;

import org.springframework.scheduling.support.CronExpression;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class EtlTimeUtils {

    public static OffsetDateTime getTriggerTime() {
        return OffsetDateTime
                .now()
                .truncatedTo(ChronoUnit.SECONDS)
                .withSecond((OffsetDateTime.now().getSecond() / 10) * 10)
                .withNano(0);
    }

    public static boolean cronCheck(String cronString, OffsetDateTime triggerTime) {
        for (String cron : cronString.split("\\|")) {
            OffsetDateTime nextExecTime = CronExpression.parse(cron).next(triggerTime.minusSeconds(5));
            if (triggerTime.equals(nextExecTime)) {
                return true;
            }
        }
        return false;
    }

}
