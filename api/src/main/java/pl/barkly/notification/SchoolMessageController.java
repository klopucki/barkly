package pl.barkly.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.school.SchoolService;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserRole;
import pl.barkly.user.UserService;

import java.util.List;

@RestController
class SchoolMessageController {
    private final SchoolService schools;
    private final TrainingRepository trainings;
    private final NotificationService notifications;
    private final BookingRepository bookings;
    private final UserService users;

    SchoolMessageController(
            SchoolService schools,
            TrainingRepository trainings,
            NotificationService notifications,
            BookingRepository bookings,
            UserService users) {
        this.schools = schools;
        this.trainings = trainings;
        this.notifications = notifications;
        this.bookings = bookings;
        this.users = users;
    }

    record Message(@NotBlank @Size(max = 1000) String message) {}

    @GetMapping("/api/schools/{schoolId}/messages")
    @Transactional(readOnly = true)
    List<NotificationService.SchoolMessageItem> schoolMessages(
            @PathVariable Long schoolId, @RequestParam(defaultValue = "0") int page) {
        schools.requireManagementAccess(schools.findEntityWithOwner(schoolId));
        return notifications.schoolMessages(schoolId, page);
    }

    @PostMapping("/api/schools/{schoolId}/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void schoolMessage(@PathVariable Long schoolId, @Valid @RequestBody Message request) {
        var school = schools.findEntityWithOwner(schoolId);
        schools.requireManagementAccess(school);
        notifications.messageSchool(
                schoolId, request.message().trim(), "/schools/" + school.getSlug());
    }

    @GetMapping("/api/trainings/{trainingId}/messages")
    @Transactional(readOnly = true)
    List<NotificationService.TrainingMessageItem> trainingMessages(
            @PathVariable Long trainingId, @RequestParam(defaultValue = "0") int page) {
        var user = users.currentUser();
        var training =
                trainings
                        .findByIdAndDeletedAtIsNull(trainingId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var school = schools.findEntityWithOwner(training.toResponse().schoolId());
        boolean manager =
                user.getRole() == UserRole.SUPER_ADMIN
                        || (user.getRole() == UserRole.SCHOOL_ADMIN
                                && school.getOwner().getId().equals(user.getId()));
        if (!manager
                && !bookings.existsByTrainingIdAndUserIdAndDeletedAtIsNull(
                        trainingId, user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return notifications.trainingMessages(trainingId, page);
    }

    @PostMapping("/api/trainings/{trainingId}/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void trainingMessage(@PathVariable Long trainingId, @Valid @RequestBody Message request) {
        var training =
                trainings
                        .findByIdAndDeletedAtIsNull(trainingId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        schools.requireManagementAccess(
                schools.findEntityWithOwner(training.toResponse().schoolId()));
        notifications.messageTraining(trainingId, request.message().trim());
    }
}
