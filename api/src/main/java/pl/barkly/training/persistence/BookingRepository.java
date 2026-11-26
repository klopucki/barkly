package pl.barkly.training.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.barkly.home.HomeSummary.PersonalBooking;
import pl.barkly.home.HomeSummary.RecentRegistration;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query(
            """
            select new pl.barkly.home.HomeSummary$PersonalBooking(b.id, t.id, t.title, b.dogName, t.startAt)
            from BookingEntity b join TrainingEntity t on t.id = b.trainingId
            join DogEntity d on d.id = b.dogId
            where b.deletedAt is null and t.deletedAt is null and t.startAt >= :now
              and (d.owner.id = :userId or exists (select i.id from DogAccessInvitation i
                where i.dogId = d.id and i.inviteeUserId = :userId and i.status = pl.barkly.user.access.AccessStatus.ACCEPTED))
            order by t.startAt asc, b.id asc
            """)
    List<PersonalBooking> findUpcomingForAccessibleDogs(
            Long userId, LocalDateTime now, Pageable pageable);

    @Query(
            """
            select new pl.barkly.home.HomeSummary$RecentRegistration(t.id, t.title, s.name, b.ownerName, b.dogName, b.createdAt)
            from BookingEntity b join TrainingEntity t on t.id = b.trainingId
            join SchoolEntity s on s.id = t.schoolId
            where b.deletedAt is null and t.deletedAt is null and t.startAt >= :now
              and ((s.owner.id = :userId and s.owner.role in (pl.barkly.user.UserRole.SCHOOL_ADMIN, pl.barkly.user.UserRole.SUPER_ADMIN)) or exists (select i.id from SchoolAccessInvitation i
                where i.schoolId = s.id and i.inviteeUserId = :userId and i.status = pl.barkly.user.access.AccessStatus.ACCEPTED))
            order by b.createdAt desc, b.id desc
            """)
    List<RecentRegistration> findRecentForManagedSchools(
            Long userId, LocalDateTime now, Pageable pageable);

    List<BookingEntity> findAllByTrainingIdAndDeletedAtIsNull(Long trainingId);

    int countByTrainingIdAndDeletedAtIsNull(Long trainingId);

    boolean existsByTrainingIdAndUserIdAndDeletedAtIsNull(Long trainingId, Long userId);

    @Query(
            """
            select case when count(booking) > 0 then true else false end
            from BookingEntity booking, DogEntity dog
            where booking.trainingId = :trainingId and booking.deletedAt is null
              and booking.dogId = dog.id
              and (dog.owner.id = :userId or exists (
                  select invitation.id from DogAccessInvitation invitation
                  where invitation.dogId = dog.id and invitation.inviteeUserId = :userId
                    and invitation.status = pl.barkly.user.access.AccessStatus.ACCEPTED))
            """)
    boolean existsForAccessibleDog(Long trainingId, Long userId);

    boolean existsByTrainingIdAndDogIdAndDeletedAtIsNull(Long trainingId, Long dogId);

    List<BookingEntity> findAllByDogIdInAndDeletedAtIsNull(List<Long> dogIds);

    @Modifying
    @Query(
            """
                update BookingEntity b
                set b.deletedAt = :deletedAt
                where b.trainingId = :trainingId
                  and b.deletedAt is null
            """)
    void softDeleteByTrainingId(Long trainingId, LocalDateTime deletedAt);
}
