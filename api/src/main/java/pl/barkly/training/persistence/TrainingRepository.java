package pl.barkly.training.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.barkly.home.HomeSummary.SchoolTraining;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {

    @Query(
            """
            select training from TrainingEntity training
            where training.deletedAt is null
            order by training.startAt desc, training.id desc
            """)
    List<TrainingEntity> findAllActiveOrdered();

    @Query(
            """
            select new pl.barkly.home.HomeSummary$SchoolTraining(t.id, t.title, t.startAt, t.capacity, count(b.id), s.id, s.name)
            from TrainingEntity t join SchoolEntity s on s.id = t.schoolId
            left join BookingEntity b on b.trainingId = t.id and b.deletedAt is null
            where t.deletedAt is null and t.startAt >= :now and
              ((s.owner.id = :userId and s.owner.role in (pl.barkly.user.UserRole.SCHOOL_ADMIN, pl.barkly.user.UserRole.SUPER_ADMIN)) or exists (select i.id from SchoolAccessInvitation i
                where i.schoolId = s.id and i.inviteeUserId = :userId and i.status = pl.barkly.user.access.AccessStatus.ACCEPTED))
            group by t.id, t.title, t.startAt, t.capacity, s.id, s.name
            order by t.startAt asc, t.id asc
            """)
    List<SchoolTraining> findUpcomingForManagedSchools(
            Long userId, LocalDateTime now, Pageable pageable);

    Optional<TrainingEntity> findByIdAndDeletedAtIsNull(Long id);

    List<TrainingEntity> findAllByIdInAndDeletedAtIsNull(List<Long> ids);

    @Query(
            """
            select training from TrainingEntity training
            where training.deletedAt is null
              and (:query = '' or lower(training.title) like lower(concat('%', :query, '%'))
                   or lower(training.trainerName) like lower(concat('%', :query, '%')))
            order by training.startAt desc, training.id desc
            """)
    Page<TrainingEntity> search(@Param("query") String query, Pageable pageable);

    @Query(
            """
            select training from TrainingEntity training
            where training.deletedAt is null and training.id in :ids
              and (:query = '' or lower(training.title) like lower(concat('%', :query, '%')) or lower(training.trainerName) like lower(concat('%', :query, '%')))
            order by training.startAt desc, training.id desc
            """)
    Page<TrainingEntity> searchFavorites(
            @Param("query") String query, @Param("ids") List<Long> ids, Pageable pageable);
}
