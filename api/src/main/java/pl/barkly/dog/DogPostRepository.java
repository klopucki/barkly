package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface DogPostRepository extends JpaRepository<DogPostEntity, Long> {
    @Query("select post from DogPostEntity post join fetch post.dog dog join fetch dog.owner order by post.publishedAt desc")
    List<DogPostEntity> findFeed();

    @Query("select post from DogPostEntity post join fetch post.dog dog join fetch dog.owner where post.id = :id")
    Optional<DogPostEntity> findWithDogById(Long id);
}
