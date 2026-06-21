package pl.barkly.training;

import org.springframework.data.jpa.repository.JpaRepository;

interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {
}