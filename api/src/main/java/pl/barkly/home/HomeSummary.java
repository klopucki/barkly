package pl.barkly.home;

import java.time.LocalDateTime;
import java.util.List;

public record HomeSummary(
        boolean managesSchools,
        long pendingInvitations,
        List<PersonalBooking> personalBookings,
        List<SchoolTraining> schoolTrainings,
        List<RecentRegistration> recentRegistrations) {
    public record PersonalBooking(
            Long bookingId,
            Long trainingId,
            String trainingTitle,
            String dogName,
            LocalDateTime startAt) {}

    public record SchoolTraining(
            Long trainingId,
            String title,
            LocalDateTime startAt,
            Integer capacity,
            Long bookedCount,
            Long schoolId,
            String schoolName) {}

    public record RecentRegistration(
            Long trainingId,
            String trainingTitle,
            String schoolName,
            String ownerName,
            String dogName,
            LocalDateTime createdAt) {}
}
