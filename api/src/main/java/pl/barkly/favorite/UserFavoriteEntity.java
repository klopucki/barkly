package pl.barkly.favorite;

import jakarta.persistence.*;
import pl.barkly.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_type", "target_id"}))
public class UserFavoriteEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private UserEntity user;
    @Enumerated(EnumType.STRING) @Column(name = "target_type", nullable = false) private FavoriteType targetType;
    @Column(name = "target_id", nullable = false) private Long targetId;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected UserFavoriteEntity() { }
    UserFavoriteEntity(UserEntity user, FavoriteType targetType, Long targetId) {
        this.user = user; this.targetType = targetType; this.targetId = targetId; this.createdAt = LocalDateTime.now();
    }
    public FavoriteType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
}
