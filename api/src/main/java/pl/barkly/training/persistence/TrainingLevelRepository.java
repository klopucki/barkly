package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingLevelRepository extends JpaRepository<TrainingLevelEntity, Long> {
    @EntityGraph(attributePaths = "trainingType")
    List<TrainingLevelEntity> findAllByActiveTrueOrderByName();
}
