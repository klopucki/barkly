package pl.barkly.training;

import jakarta.persistence.*;

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

    protected TrainingEntity() {
    }

    TrainingEntity(String title, String description, TrainingLevel level, LocalDate date, int capacity, String trainerName) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.date = date;
        this.capacity = capacity;
        this.trainerName = trainerName;
    }

    Training toDomain() {
        return new Training(id, title, description, level, date, capacity);
    }

    static TrainingEntity from(TrainingCreateRequest request) {
        return new TrainingEntity(
                request.title(),
                request.description(),
                request.level(),
                request.startAt(),
                request.capacity(),
                request.trainerName()

        );
    }
}