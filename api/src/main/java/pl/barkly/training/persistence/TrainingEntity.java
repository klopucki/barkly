package pl.barkly.training.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pl.barkly.training.api.DictionaryValueResponse;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id")
    private TrainingTypeEntity trainingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_level_id")
    private TrainingLevelEntity trainingLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_group_id")
    private TargetGroupEntity targetGroup;

    private boolean homeVisit;

    private LocalDateTime startAt;

    private Integer capacity;

    private String description;

    private String imageKey;

    private LocalDateTime deletedAt;

    public TrainingEntity(){}

    public TrainingEntity(
            TrainingCreateRequest request,
            TrainingTypeEntity trainingType,
            TrainingLevelEntity trainingLevel,
            TargetGroupEntity targetGroup
    ) {
        this.schoolId = request.schoolId();
        this.title = request.title();
        this.description = request.description() == null ? "" : request.description();
        this.trainingType = trainingType;
        this.trainingLevel = trainingLevel;
        this.targetGroup = targetGroup;
        this.homeVisit = request.homeVisit();
        this.startAt = request.startAt();
        this.capacity = request.capacity();
        this.trainerName = request.trainerName();
    }

    public Long getId() {
        return id;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
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
                dictionaryValue(trainingType),
                trainingLevel == null ? null : dictionaryValue(trainingLevel),
                targetGroup == null ? null : dictionaryValue(targetGroup),
                homeVisit,
                startAt,
                capacity,
                bookedCount,
                imageKey
        );
    }

    private DictionaryValueResponse dictionaryValue(TrainingTypeEntity value) {
        return new DictionaryValueResponse(value.getId(), value.getCode(), value.getName());
    }

    private DictionaryValueResponse dictionaryValue(TrainingLevelEntity value) {
        return new DictionaryValueResponse(value.getId(), value.getCode(), value.getName());
    }

    private DictionaryValueResponse dictionaryValue(TargetGroupEntity value) {
        return new DictionaryValueResponse(value.getId(), value.getCode(), value.getName());
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void update(
            TrainingCreateRequest request,
            TrainingTypeEntity trainingType,
            TrainingLevelEntity trainingLevel,
            TargetGroupEntity targetGroup
    ) {
        this.schoolId = request.schoolId();
        this.title = request.title();
        this.trainerName = request.trainerName();
        this.trainingType = trainingType;
        this.trainingLevel = trainingLevel;
        this.targetGroup = targetGroup;
        this.homeVisit = request.homeVisit();
        this.startAt = request.startAt();
        this.capacity = request.capacity();
        this.description = request.description() == null ? "" : request.description();
    }

    public void validateBooking(int bookedCount) {
        if (capacity != null && bookedCount >= capacity) {
            throw new TrainingCapacityExceededException(id);
        }
    }
}
