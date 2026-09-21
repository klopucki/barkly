package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface DogPostCommentRepository extends JpaRepository<DogPostCommentEntity, Long> {
    List<DogPostCommentEntity> findAllByPost_IdOrderByPublishedAtAsc(Long postId);
    long countByPost_Id(Long postId);
}
