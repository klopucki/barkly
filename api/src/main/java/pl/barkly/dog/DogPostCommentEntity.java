package pl.barkly.dog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import pl.barkly.user.UserEntity;

@Entity
@Table(name = "dog_post_comment")
class DogPostCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private DogPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    protected DogPostCommentEntity() {
    }

    DogPostCommentEntity(DogPostEntity post, UserEntity user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.publishedAt = LocalDateTime.now();
    }

    Long getId() {
        return id;
    }

    UserEntity getUser() {
        return user;
    }

    String getContent() {
        return content;
    }

    LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
