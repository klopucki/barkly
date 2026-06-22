package pl.barkly.training;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "training")
class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TrainingLevel level;

    @Column(name = "start_at")
    private LocalDate date;

    private int capacity;

    private String trainerName;

    TrainingEntity(String title,
                   String description,
                   TrainingLevel level,
                   LocalDate localDate,
                   int capacity,
                   String trainerName) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.date = localDate;
        this.capacity = capacity;
        this.trainerName = trainerName;
    }

    public TrainingEntity() {

    }

    static TrainingEntity create(TrainingCreateRequest request) {
        return new TrainingEntity(
                request.title(),
                request.description(),
                request.level(),
                request.startAt(),
                request.capacity(),
                request.trainerName()
        );
    }

    TrainingResponse toResponse() {
        return toResponse(0);
    }

    TrainingResponse toResponse(int bookedCount) {
        return new TrainingResponse(
                id,
                title,
                trainerName,
                description,
                level,
                date,
                capacity,
                bookedCount
        );
    }
}