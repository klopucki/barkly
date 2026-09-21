package pl.barkly.training.persistence;

import jakarta.persistence.*;
import pl.barkly.user.UserEntity;

@Entity
@Table(name = "training_paw_reaction", uniqueConstraints = @UniqueConstraint(columnNames = {"training_id", "user_id"}))
public class TrainingPawReactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "training_id", nullable = false) private Long trainingId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private UserEntity user;
    protected TrainingPawReactionEntity() { }
    public TrainingPawReactionEntity(Long trainingId, UserEntity user) { this.trainingId = trainingId; this.user = user; }
}
