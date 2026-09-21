package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

interface DogPostReactionRepository extends JpaRepository<DogPostReactionEntity, Long> {
    Optional<DogPostReactionEntity> findByPost_IdAndUser_Id(Long postId, Long userId);
    List<DogPostReactionEntity> findAllByPost_Id(Long postId);
}
