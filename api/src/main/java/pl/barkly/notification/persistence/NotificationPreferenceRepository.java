package pl.barkly.notification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreferenceEntity, Long> {
    // Atomic upsert handles simultaneous first-time preference updates from separate sessions.
    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO notification_preference(user_id, reminders_enabled) VALUES (:userId, :enabled)
                    ON CONFLICT (user_id) DO UPDATE SET reminders_enabled = EXCLUDED.reminders_enabled
                    """,
            nativeQuery = true)
    void setRemindersEnabled(Long userId, boolean enabled);
}
