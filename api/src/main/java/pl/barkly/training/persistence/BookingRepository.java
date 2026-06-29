package pl.barkly.training.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByTrainingId(Long trainingId);

    int countByTrainingId(Long trainingId);
}