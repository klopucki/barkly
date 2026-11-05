package pl.barkly.notification.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_message")
public class TrainingMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainingId;
    private String message;
    private LocalDateTime createdAt;

    protected TrainingMessageEntity() {}

    public TrainingMessageEntity(Long trainingId, String message, LocalDateTime createdAt) {
        this.trainingId = trainingId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
