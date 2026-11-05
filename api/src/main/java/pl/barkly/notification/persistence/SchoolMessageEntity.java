package pl.barkly.notification.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_message")
public class SchoolMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long schoolId;
    private String message;
    private String link;
    private LocalDateTime createdAt;

    protected SchoolMessageEntity() {}

    public SchoolMessageEntity(
            Long schoolId, String message, String link, LocalDateTime createdAt) {
        this.schoolId = schoolId;
        this.message = message;
        this.link = link;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
