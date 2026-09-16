package pl.barkly.school.news;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface SchoolNewsRepository extends JpaRepository<SchoolNewsEntity, Long> {
    @Override
    @EntityGraph(attributePaths = {"school", "author"})
    Optional<SchoolNewsEntity> findById(Long id);

    @Query("select news from SchoolNewsEntity news join fetch news.school join fetch news.author where news.id = :id")
    Optional<SchoolNewsEntity> findWithDetailsById(@Param("id") Long id);

    @Query("select news from SchoolNewsEntity news join fetch news.school join fetch news.author where news.active = true order by news.publishedAt desc")
    List<SchoolNewsEntity> findAllByOrderByPublishedAtDesc();

    @Query("""
            select news from SchoolNewsEntity news
            join fetch news.school
            join fetch news.author
            where news.school.id = :schoolId and news.active = true
            order by news.publishedAt desc
            """)
    List<SchoolNewsEntity> findAllBySchoolIdOrderByPublishedAtDesc(@Param("schoolId") Long schoolId);

    @Query("select news from SchoolNewsEntity news join fetch news.school join fetch news.author where news.school.id = :schoolId order by news.publishedAt desc")
    List<SchoolNewsEntity> findAllManagedBySchoolIdOrderByPublishedAtDesc(@Param("schoolId") Long schoolId);

    @Query("""
            select news from SchoolNewsEntity news
            where news.active = true and (
                :query = ''
                or lower(news.title) like lower(concat('%', :query, '%'))
                or lower(news.content) like lower(concat('%', :query, '%'))
                or lower(news.school.name) like lower(concat('%', :query, '%'))
            )
            order by news.publishedAt desc
            """)
    @EntityGraph(attributePaths = {"school", "author"})
    Page<SchoolNewsEntity> search(@Param("query") String query, Pageable pageable);

    @Query("""
            select news from SchoolNewsEntity news where news.active = true and news.id in :ids and (
                :query = '' or lower(news.title) like lower(concat('%', :query, '%'))
                or lower(news.content) like lower(concat('%', :query, '%')) or lower(news.school.name) like lower(concat('%', :query, '%'))
            ) order by news.publishedAt desc
            """)
    @EntityGraph(attributePaths = {"school", "author"})
    Page<SchoolNewsEntity> searchFavorites(@Param("query") String query, @Param("ids") List<Long> ids, Pageable pageable);
}
