package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DogImageRepository extends JpaRepository<DogImageEntity, Long> {
    List<DogImageEntity> findAllByDog_IdOrderByIdAsc(Long dogId);
}
