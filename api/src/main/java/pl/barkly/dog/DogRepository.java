package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DogRepository extends JpaRepository<DogEntity, Long> {
    List<DogEntity> findAllByOwner_IdOrderByNameAsc(Long ownerId);

    List<DogEntity> findAllByOrderByNameAsc();
}
