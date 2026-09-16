package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    List<TrainingEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    Optional<TrainingEntity> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    @Query("""
            select training from TrainingEntity training
            left join training.trainingLevel level
            left join training.targetGroup targetGroup
            where training.deletedAt is null
              and (:query = '' or lower(training.title) like lower(concat('%', :query, '%'))
                   or lower(training.trainerName) like lower(concat('%', :query, '%'))
                   or lower(training.trainingType.name) like lower(concat('%', :query, '%'))
                   or lower(level.name) like lower(concat('%', :query, '%'))
                   or lower(targetGroup.name) like lower(concat('%', :query, '%')))
              and (:type = '' or training.trainingType.name = :type)
            """)
    Page<TrainingEntity> search(@Param("query") String query, @Param("type") String type, Pageable pageable);

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    @Query("""
            select training from TrainingEntity training left join training.trainingLevel level left join training.targetGroup targetGroup
            where training.deletedAt is null and training.id in :ids
              and (:query = '' or lower(training.title) like lower(concat('%', :query, '%')) or lower(training.trainerName) like lower(concat('%', :query, '%'))
                   or lower(training.trainingType.name) like lower(concat('%', :query, '%')) or lower(level.name) like lower(concat('%', :query, '%')) or lower(targetGroup.name) like lower(concat('%', :query, '%')))
              and (:type = '' or training.trainingType.name = :type)
            """)
    Page<TrainingEntity> searchFavorites(@Param("query") String query, @Param("type") String type, @Param("ids") List<Long> ids, Pageable pageable);
}
