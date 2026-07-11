package pl.barkly.training.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_level")
public class TrainingLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id")
    private TrainingTypeEntity trainingType;

    protected TrainingLevelEntity() {
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public TrainingTypeEntity getTrainingType() { return trainingType; }
}
