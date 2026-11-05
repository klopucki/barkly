package pl.barkly.training.application;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.school.SchoolService;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.time.LocalDateTime;

@Service
class TrainingCommandService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;
    private final TrainingImageStorage imageStorage;
    private final SchoolService schoolService;
    private final pl.barkly.notification.NotificationService notifications;

    TrainingCommandService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository,
            TrainingImageStorage imageStorage,
            SchoolService schoolService,
            pl.barkly.notification.NotificationService notifications) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
        this.imageStorage = imageStorage;
        this.schoolService = schoolService;
        this.notifications = notifications;
    }

    @Transactional
    TrainingResponse create(TrainingCreateRequest request) {
        schoolService.requireManagementAccess(
                schoolService.findEntityWithOwner(request.schoolId()));
        TrainingEntity saved = trainingRepository.save(new TrainingEntity(request));
        if (Boolean.TRUE.equals(request.notifySchool()))
            notifications.announceTraining(request.schoolId(), saved);
        return saved.toResponse(0);
    }

    @Transactional
    TrainingResponse update(Long id, TrainingCreateRequest request) {
        TrainingEntity training =
                trainingRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        schoolService.requireManagementAccess(
                schoolService.findEntityWithOwner(training.toResponse().schoolId()));
        if (!training.toResponse().schoolId().equals(request.schoolId())) {
            schoolService.requireManagementAccess(
                    schoolService.findEntityWithOwner(request.schoolId()));
        }
        boolean changed = training.notificationDetailsChanged(request);
        boolean dateChanged = !training.getStartAt().equals(request.startAt());
        training.update(request);
        if (changed) notifications.trainingChanged(training, false, dateChanged);
        int bookedCount = bookingRepository.countByTrainingIdAndDeletedAtIsNull(id);
        return training.toResponse(bookedCount);
    }

    @Transactional
    public void deleteTraining(Long id) {
        TrainingEntity training =
                trainingRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        schoolService.requireManagementAccess(
                schoolService.findEntityWithOwner(training.toResponse().schoolId()));

        LocalDateTime deletedAt = LocalDateTime.now();

        notifications.trainingChanged(training, true, false);
        training.softDelete();
        bookingRepository.softDeleteByTrainingId(id, deletedAt);
        imageStorage.delete(training.getImageKey());
    }
}
