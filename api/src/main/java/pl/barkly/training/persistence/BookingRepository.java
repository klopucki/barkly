package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findAllByTrainingIdAndDeletedAtIsNull(Long trainingId);

    int countByTrainingId(Long trainingId);

    @Modifying
    @Query("""
                update BookingEntity b
                set b.deletedAt = :deletedAt
                where b.trainingId = :trainingId
                  and b.deletedAt is null
            """)
    void softDeleteByTrainingId(Long trainingId, LocalDateTime deletedAt);
}