package de.trustable.ca3s.core.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author kuehn
 *
 */
@Component
public class ProtectedContentCleanupScheduler {

    private final int maxRecordsPerTransaction;

    final private ProtectedContentScheduleUtil protectedContentScheduleUtil;

    public ProtectedContentCleanupScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                                            ProtectedContentScheduleUtil protectedContentScheduleUtil) {
        this.protectedContentScheduleUtil = protectedContentScheduleUtil;
        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
    }

    @Scheduled(fixedRateString="${ca3s.schedule.rate.protectedContentCleanup:600000}")
	public void runDaily() {

        protectedContentScheduleUtil.invalidateCSRsWithExpiringPC(maxRecordsPerTransaction);
        protectedContentScheduleUtil.deleteExpiredElements(maxRecordsPerTransaction);
	}

}
