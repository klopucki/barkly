package pl.barkly.school.news.api;
import pl.barkly.school.news.SchoolNewsEntity;
import java.time.LocalDateTime;
public record SchoolNewsResponse(Long id, Long schoolId, String schoolName, Long authorId, String authorDisplayName, String title, String content, boolean active, LocalDateTime publishedAt) {
 public static SchoolNewsResponse from(SchoolNewsEntity n){return new SchoolNewsResponse(n.getId(),n.getSchoolId(),n.getSchoolName(),n.getAuthorId(),n.getAuthorDisplayName(),n.getTitle(),n.getContent(),n.isActive(),n.getPublishedAt());}
}
