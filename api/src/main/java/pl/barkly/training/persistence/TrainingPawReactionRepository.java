package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrainingPawReactionRepository extends JpaRepository<TrainingPawReactionEntity, Long> {
    Optional<TrainingPawReactionEntity> findByTrainingIdAndUser_Id(Long trainingId, Long userId);
    long countByTrainingId(Long trainingId);
}
