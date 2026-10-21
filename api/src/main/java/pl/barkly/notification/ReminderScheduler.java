package pl.barkly.notification;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
class ReminderScheduler {
    private final NotificationService notifications;

    ReminderScheduler(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Scheduled(fixedDelayString = "${barkly.notifications.reminder-interval-ms:60000}")
    public void sendReminders() {
        notifications.createReminders(LocalDateTime.now());
    }
}
