package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {

    List<TrainingEntity> findAllByDeletedAtIsNull();

    Optional<TrainingEntity> findByIdAndDeletedAtIsNull(Long id);
}