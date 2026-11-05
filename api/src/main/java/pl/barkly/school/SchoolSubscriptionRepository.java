package pl.barkly.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SchoolSubscriptionRepository
        extends JpaRepository<SchoolSubscription, SchoolSubscription.Key> {
    boolean existsByUserIdAndSchoolId(Long userId, Long schoolId);

    @Modifying
    @Query(
            value =
                    "INSERT INTO school_subscription (user_id, school_id) VALUES (:userId,"
                            + " :schoolId) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void subscribe(Long userId, Long schoolId);

    @Modifying
    @Query("DELETE FROM SchoolSubscription s WHERE s.userId = :userId AND s.schoolId = :schoolId")
    void unsubscribe(Long userId, Long schoolId);
}
