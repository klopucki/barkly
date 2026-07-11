package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    List<TrainingEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"trainingType", "trainingLevel", "targetGroup"})
    Optional<TrainingEntity> findByIdAndDeletedAtIsNull(Long id);
}
