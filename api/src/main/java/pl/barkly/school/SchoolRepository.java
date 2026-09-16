package pl.barkly.school;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<SchoolEntity, Long> {
    List<SchoolEntity> findAllByOrderByNameAsc();
    List<SchoolEntity> findAllByOwnerIdOrderByNameAsc(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    Optional<SchoolEntity> findBySlug(String slug);

    @EntityGraph(attributePaths = "owner")
    Optional<SchoolEntity> findWithOwnerById(Long id);

    @Query("""
            select school from SchoolEntity school
            where :query = ''
               or lower(school.name) like lower(concat('%', :query, '%'))
               or lower(school.address) like lower(concat('%', :query, '%'))
               or lower(school.description) like lower(concat('%', :query, '%'))
               or lower(school.activities) like lower(concat('%', :query, '%'))
            """)
    Page<SchoolEntity> search(@Param("query") String query, Pageable pageable);

    @Query("""
            select school from SchoolEntity school
            where school.id in :ids and (:query = ''
               or lower(school.name) like lower(concat('%', :query, '%'))
               or lower(school.address) like lower(concat('%', :query, '%'))
               or lower(school.description) like lower(concat('%', :query, '%'))
               or lower(school.activities) like lower(concat('%', :query, '%')))
            """)
    Page<SchoolEntity> searchFavorites(@Param("query") String query, @Param("ids") List<Long> ids, Pageable pageable);
}
