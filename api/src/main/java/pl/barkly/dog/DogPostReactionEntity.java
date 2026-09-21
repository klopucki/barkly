package pl.barkly.dog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pl.barkly.user.UserEntity;

@Entity
@Table(name = "dog_post_reaction", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
class DogPostReactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private DogPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private PostReactionType reactionType;

    protected DogPostReactionEntity() {
    }

    DogPostReactionEntity(DogPostEntity post, UserEntity user, PostReactionType reactionType) {
        this.post = post;
        this.user = user;
        this.reactionType = reactionType;
    }

    PostReactionType getReactionType() {
        return reactionType;
    }

    UserEntity getUser() {
        return user;
    }

    void setReactionType(PostReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
