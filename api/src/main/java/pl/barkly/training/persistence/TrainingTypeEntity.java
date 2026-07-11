package pl.barkly.training.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "training_type")
public class TrainingTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private boolean active;

    protected TrainingTypeEntity() {
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
