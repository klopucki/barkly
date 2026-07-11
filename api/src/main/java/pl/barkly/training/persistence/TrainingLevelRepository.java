package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface TrainingLevelRepository extends JpaRepository<TrainingLevelEntity, Long> {
    @EntityGraph(attributePaths = "trainingType")
    List<TrainingLevelEntity> findAllByActiveTrueOrderByName();
}
