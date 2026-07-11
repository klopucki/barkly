package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, Long> {
    List<TrainingTypeEntity> findAllByActiveTrueOrderByName();
}
