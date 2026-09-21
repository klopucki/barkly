package pl.barkly.training.persistence;

import jakarta.persistence.*;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id")
    private Long trainingId;

    @Column(name = "owner_name")
    private String ownerName;

    private String email;

    @Column(name = "dog_name")
    private String dogName;

    @Column(name = "dog_id")
    private Long dogId;

    @Column(name = "dog_age")
    private int dogAge;

    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    protected BookingEntity() {
    }

    public BookingEntity(Long trainingId, BookingCreateRequest request) {
        this.trainingId = trainingId;
        this.ownerName = request.ownerName();
        this.email = request.email();
        this.dogName = request.dogName();
        this.dogAge = request.dogAge();
        this.notes = request.notes();
        this.createdAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public BookingEntity(Long trainingId, Long dogId, String ownerName, String email, String dogName, int dogAge) {
        this.trainingId = trainingId;
        this.dogId = dogId;
        this.ownerName = ownerName;
        this.email = email;
        this.dogName = dogName;
        this.dogAge = dogAge;
        this.notes = "";
        this.createdAt = LocalDateTime.now();
    }

    public Long getTrainingId() {
        return trainingId;
    }

    public BookingResponse toResponse() {
        return new BookingResponse(
                id,
                trainingId,
                dogId,
                ownerName,
                email,
                dogName,
                dogAge,
                notes,
                createdAt
        );
    }
}
