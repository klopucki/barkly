package pl.barkly.school;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchoolImageRepository extends JpaRepository<SchoolImageEntity, Long> {
    List<SchoolImageEntity> findAllBySchool_IdOrderByIdAsc(Long schoolId);
}
