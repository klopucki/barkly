package pl.barkly.school.news;

import jakarta.persistence.*;
import pl.barkly.school.SchoolEntity;
import pl.barkly.school.news.api.SchoolNewsRequest;
import pl.barkly.user.UserEntity;
import java.time.LocalDateTime;

@Entity @Table(name = "school_news")
public class SchoolNewsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "school_id") private SchoolEntity school;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_user_id") private UserEntity author;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String content;
    @Column(nullable = false) private boolean active;
    @Column(nullable = false) private LocalDateTime publishedAt;
    protected SchoolNewsEntity() { }
    public SchoolNewsEntity(SchoolEntity school, UserEntity author, SchoolNewsRequest request) { this.school=school; this.author=author; this.active=true; update(request); this.publishedAt=LocalDateTime.now(); }
    public Long getId(){return id;} public Long getSchoolId(){return school.getId();} public String getSchoolName(){return school.getName();} public Long getAuthorId(){return author.getId();} public String getAuthorDisplayName(){return author.getDisplayName();} public String getTitle(){return title;} public String getContent(){return content;} public boolean isActive(){return active;} public LocalDateTime getPublishedAt(){return publishedAt;}
    public void update(SchoolNewsRequest request){title=request.title().trim();content=request.content().trim();if(request.active()!=null)active=request.active();}
}
