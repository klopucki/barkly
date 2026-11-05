package pl.barkly.school;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import pl.barkly.user.UserService;

@RestController
@RequestMapping("/api/my/school-subscriptions/{schoolId}")
class SchoolSubscriptionController {
    private final SchoolSubscriptionRepository subscriptions;
    private final SchoolService schools;
    private final UserService users;

    SchoolSubscriptionController(
            SchoolSubscriptionRepository subscriptions, SchoolService schools, UserService users) {
        this.subscriptions = subscriptions;
        this.schools = schools;
        this.users = users;
    }

    @GetMapping
    boolean subscribed(@PathVariable Long schoolId) {
        schools.findEntityWithOwner(schoolId);
        return subscriptions.existsByUserIdAndSchoolId(users.currentUser().getId(), schoolId);
    }

    @PutMapping
    @Transactional
    void subscribe(@PathVariable Long schoolId) {
        schools.findEntityWithOwner(schoolId);
        subscriptions.subscribe(users.currentUser().getId(), schoolId);
    }

    @DeleteMapping
    @Transactional
    void unsubscribe(@PathVariable Long schoolId) {
        schools.findEntityWithOwner(schoolId);
        subscriptions.unsubscribe(users.currentUser().getId(), schoolId);
    }
}
