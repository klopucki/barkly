package pl.barkly.home;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.barkly.school.SchoolRepository;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserService;
import pl.barkly.user.access.SharedAccessService;

import java.time.LocalDateTime;

@RestController
class HomeController {
    private final BookingRepository bookings;
    private final TrainingRepository trainings;
    private final UserService users;
    private final SchoolRepository schools;
    private final SharedAccessService access;

    HomeController(
            BookingRepository bookings,
            TrainingRepository trainings,
            UserService users,
            SchoolRepository schools,
            SharedAccessService access) {
        this.bookings = bookings;
        this.trainings = trainings;
        this.users = users;
        this.schools = schools;
        this.access = access;
    }

    @GetMapping("/api/my/home")
    @Transactional(readOnly = true)
    HomeSummary summary() {
        Long userId = users.currentUser().getId();
        var now = LocalDateTime.now();
        var limit = PageRequest.of(0, 4);
        return new HomeSummary(
                schools.existsManagedByUserId(userId),
                access.pendingCount(userId),
                bookings.findUpcomingForAccessibleDogs(userId, now, limit),
                trainings.findUpcomingForManagedSchools(userId, now, limit),
                bookings.findRecentForManagedSchools(userId, now, limit));
    }
}
