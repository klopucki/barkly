package pl.barkly.notification;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.barkly.notification.persistence.NotificationPreferenceEntity;
import pl.barkly.notification.persistence.NotificationPreferenceRepository;
import pl.barkly.notification.persistence.NotificationRepository;
import pl.barkly.training.persistence.BookingEntity;
import pl.barkly.training.persistence.TrainingEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notifications;
    private final NotificationPreferenceRepository preferences;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationService(
            NotificationRepository notifications, NotificationPreferenceRepository preferences) {
        this.notifications = notifications;
        this.preferences = preferences;
    }

    public void bookingConfirmed(Long userId, BookingEntity booking, TrainingEntity training) {
        notifications.insertBookingConfirmation(
                userId,
                training.getId(),
                "booking:" + booking.getId(),
                "Zapisano psa "
                        + booking.getDogName()
                        + " na „"
                        + training.getTitle()
                        + "” — "
                        + DATE.format(training.getStartAt())
                        + ".",
                "/trainings/" + training.getId(),
                LocalDateTime.now());
    }

    public void trainingChanged(TrainingEntity training, boolean cancelled, boolean dateChanged) {
        notifications.lockReminderDelivery();
        if (cancelled || dateChanged) {
            notifications.deleteUnreadReminders(training.getId());
        }
        String message =
                cancelled
                        ? "Odwołano trening „"
                                + training.getTitle()
                                + "” — "
                                + DATE.format(training.getStartAt())
                                + "."
                        : "Zmieniono szczegóły treningu „"
                                + training.getTitle()
                                + "”. Aktualny termin: "
                                + DATE.format(training.getStartAt())
                                + ". Sprawdź szczegóły.";
        notifications.insertForTrainingParticipants(
                training.getId(),
                "training:" + UUID.randomUUID(),
                cancelled ? "TRAINING_CANCELLED" : "TRAINING_CHANGED",
                message,
                cancelled ? "/me/calendar" : "/trainings/" + training.getId(),
                LocalDateTime.now());
        if (cancelled) {
            // Earlier notifications must not lead to a deleted training page.
            notifications.updateTrainingLinks(training.getId(), "/me/calendar");
        }
    }

    public int createReminders(LocalDateTime now) {
        notifications.lockReminderDelivery();
        return notifications.insertDueReminders(now, now.plusHours(24));
    }

    @Transactional(readOnly = true)
    public List<Item> list(Long userId, int page) {
        return notifications
                .findByUserIdOrderByCreatedAtDescIdDesc(
                        userId, PageRequest.of(Math.max(0, page), 30))
                .stream()
                .map(
                        notification ->
                                new Item(
                                        notification.getId(),
                                        notification.getType(),
                                        notification.getMessage(),
                                        notification.getLink(),
                                        notification.getCreatedAt(),
                                        notification.isRead()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    public boolean markRead(Long userId, Long id) {
        return notifications.markRead(userId, id, LocalDateTime.now()) > 0;
    }

    public void markAllRead(Long userId) {
        notifications.markAllRead(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean remindersEnabled(Long userId) {
        return preferences
                .findById(userId)
                .map(NotificationPreferenceEntity::isRemindersEnabled)
                .orElse(true);
    }

    public void setRemindersEnabled(Long userId, boolean enabled) {
        preferences.setRemindersEnabled(userId, enabled);
    }

    public record Item(
            Long id,
            String type,
            String message,
            String link,
            LocalDateTime createdAt,
            boolean read) {}
}
