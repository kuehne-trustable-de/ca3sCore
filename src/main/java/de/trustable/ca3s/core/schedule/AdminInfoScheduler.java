package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class AdminInfoScheduler {

    private final NotificationService notificationService;

    public AdminInfoScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron="${ca3s.schedule.cron.expiryNotificationCron:0 15 2 * * ?}")
    public int notifyAdminOnStatus() {
        return notificationService.notifyAdminOnConnectorExpiry();
    }

}
