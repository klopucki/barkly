package pl.barkly.dog;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dog_post")
class DogPostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "dog_id") private DogEntity dog;
    @Column(nullable = false, length = 1000) private String content;
    private String imageKey;
    @Column(nullable = false) private LocalDateTime publishedAt;

    protected DogPostEntity() { }

    DogPostEntity(DogEntity dog, String content, String imageKey) {
        this.dog = dog;
        this.content = content;
        this.imageKey = imageKey;
        this.publishedAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    DogEntity getDog() { return dog; }
    String getContent() { return content; }
    String getImageKey() { return imageKey; }
    LocalDateTime getPublishedAt() { return publishedAt; }
}
