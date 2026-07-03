package pl.barkly.training.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pl.barkly.training.TrainingLevel;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.exceptions.TrainingCapacityExceededException;

import java.time.LocalDateTime;

@Entity
@Table(name = "training")
public class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long schoolId;

    private String title;

    private String trainerName;

    @Enumerated(EnumType.STRING)
    private TrainingLevel level;

    private LocalDateTime startAt;

    private Integer capacity;

    private String description;

    public TrainingEntity(){}

    public TrainingEntity(TrainingCreateRequest request) {
        this.title = request.title();
        this.description = request.description();
        this.level = request.level();
        this.startAt = request.startAt();
        this.capacity = request.capacity();
        this.trainerName = request.trainerName();
    }

    public TrainingResponse toResponse() {
        return toResponse(0);
    }

    public TrainingResponse toResponse(int bookedCount) {
        return new TrainingResponse(
                id,
                schoolId,
                title,
                trainerName,
                level,
                startAt,
                capacity,
                bookedCount
        );
    }

    public void validateBooking(int bookedCount) {
        if (capacity != null && bookedCount >= capacity) {
            throw new TrainingCapacityExceededException(id);
        }
    }

    public Long getId() {
        return id;
    }
}