package pl.barkly.notification.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preference")
public class NotificationPreferenceEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reminders_enabled", nullable = false)
    private boolean remindersEnabled;

    protected NotificationPreferenceEntity() {}

    public boolean isRemindersEnabled() {
        return remindersEnabled;
    }
}
