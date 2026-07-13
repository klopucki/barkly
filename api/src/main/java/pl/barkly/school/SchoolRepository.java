package pl.barkly.school;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<SchoolEntity, Long> {
    List<SchoolEntity> findAllByOrderByNameAsc();
    List<SchoolEntity> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    Optional<SchoolEntity> findBySlug(String slug);

    @EntityGraph(attributePaths = "owner")
    Optional<SchoolEntity> findWithOwnerById(Long id);
}
