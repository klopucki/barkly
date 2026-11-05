package pl.barkly.notification.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolMessageRepository extends JpaRepository<SchoolMessageEntity, Long> {
    List<SchoolMessageEntity> findBySchoolIdOrderByCreatedAtDescIdDesc(
            Long schoolId, Pageable page);
}
