package pl.barkly.training.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
