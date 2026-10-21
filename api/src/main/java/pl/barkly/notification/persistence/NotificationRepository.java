package pl.barkly.notification.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(Long userId);

    @Modifying
    @Query(
            """
            update NotificationEntity n set n.readAt = coalesce(n.readAt, :readAt)
            where n.id = :id and n.userId = :userId
            """)
    int markRead(Long userId, Long id, LocalDateTime readAt);

    @Modifying
    @Query(
            """
            update NotificationEntity n set n.readAt = :readAt
            where n.userId = :userId and n.readAt is null
            """)
    void markAllRead(Long userId, LocalDateTime readAt);

    @Modifying
    @Query(
            """
            delete from NotificationEntity n
            where n.trainingId = :trainingId and n.type = 'REMINDER' and n.readAt is null
            """)
    void deleteUnreadReminders(Long trainingId);

    @Modifying
    @Query("update NotificationEntity n set n.link = :link where n.trainingId = :trainingId")
    void updateTrainingLinks(Long trainingId, String link);

    // Transaction-scoped lock shared by delivery and training changes across application instances.
    @Query(value = "SELECT 1 FROM pg_advisory_xact_lock(470017)", nativeQuery = true)
    int lockReminderDelivery();

    // PostgreSQL ON CONFLICT preserves idempotency without an unsafe exists-then-insert race.
    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO notification(user_id, training_id, event_key, type, message, link, created_at)
                    VALUES (:userId, :trainingId, :eventKey, 'BOOKING_CONFIRMED', :message, :link, :createdAt)
                    ON CONFLICT (user_id, event_key) DO NOTHING
                    """,
            nativeQuery = true)
    void insertBookingConfirmation(
            Long userId,
            Long trainingId,
            String eventKey,
            String message,
            String link,
            LocalDateTime createdAt);

    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO notification(user_id, training_id, event_key, type, message, link, created_at)
                    SELECT DISTINCT b.user_id, b.training_id, :eventKey, :type, :message, :link, CAST(:createdAt AS timestamp)
                    FROM booking b
                    WHERE b.training_id = :trainingId AND b.deleted_at IS NULL AND b.user_id IS NOT NULL
                    ON CONFLICT (user_id, event_key) DO NOTHING
                    """,
            nativeQuery = true)
    void insertForTrainingParticipants(
            Long trainingId,
            String eventKey,
            String type,
            String message,
            String link,
            LocalDateTime createdAt);

    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO notification(user_id, training_id, event_key, type, message, link, created_at)
                    SELECT DISTINCT b.user_id, t.id, 'reminder:' || t.id || ':' || t.start_at,
                        'REMINDER', 'Przypomnienie: trening „' || t.title || '” rozpocznie się ' ||
                        to_char(t.start_at, 'DD.MM.YYYY HH24:MI') || '.', '/trainings/' || t.id, CAST(:now AS timestamp)
                    FROM booking b JOIN training t ON t.id = b.training_id
                    LEFT JOIN notification_preference p ON p.user_id = b.user_id
                    WHERE b.deleted_at IS NULL AND t.deleted_at IS NULL AND b.user_id IS NOT NULL
                      AND t.start_at > :now AND t.start_at <= :remindBefore AND COALESCE(p.reminders_enabled, TRUE)
                    ON CONFLICT (user_id, event_key) DO NOTHING
                    """,
            nativeQuery = true)
    int insertDueReminders(LocalDateTime now, LocalDateTime remindBefore);
}
