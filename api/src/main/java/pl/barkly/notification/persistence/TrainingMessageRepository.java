package pl.barkly.notification.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingMessageRepository extends JpaRepository<TrainingMessageEntity, Long> {
    List<TrainingMessageEntity> findByTrainingIdOrderByCreatedAtDescIdDesc(
            Long trainingId, Pageable pageable);
}
