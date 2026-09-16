package pl.barkly.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface UserFavoriteRepository extends JpaRepository<UserFavoriteEntity, Long> {
    List<UserFavoriteEntity> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<UserFavoriteEntity> findByUser_IdAndTargetTypeAndTargetId(Long userId, FavoriteType targetType, Long targetId);
}
