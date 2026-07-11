package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TargetGroupRepository extends JpaRepository<TargetGroupEntity, Long> {
    List<TargetGroupEntity> findAllByActiveTrueOrderByName();
}
