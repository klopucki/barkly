package pl.barkly.favorite;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.favorite.api.FavoriteResponse;
import pl.barkly.user.UserService;

import java.util.List;

@Service
public class FavoriteService {
    private final UserFavoriteRepository favorites;
    private final UserService users;

    FavoriteService(UserFavoriteRepository favorites, UserService users) {
        this.favorites = favorites;
        this.users = users;
    }

    @Transactional(readOnly = true)
    List<FavoriteResponse> mine() {
        return favorites.findAllByUser_IdOrderByCreatedAtDesc(users.currentUser().getId()).stream()
                .map(item -> new FavoriteResponse(item.getTargetType().name().toLowerCase(), item.getTargetId())).toList();
    }

    @Transactional(readOnly = true)
    public List<Long> ids(FavoriteType type) {
        return favorites.findAllByUser_IdOrderByCreatedAtDesc(users.currentUser().getId()).stream()
                .filter(item -> item.getTargetType() == type).map(UserFavoriteEntity::getTargetId).toList();
    }

    @Transactional
    void add(FavoriteType type, Long id) {
        var user = users.currentUser();
        if (favorites.findByUser_IdAndTargetTypeAndTargetId(user.getId(), type, id).isEmpty()) {
            favorites.save(new UserFavoriteEntity(user, type, id));
        }
    }

    @Transactional
    void remove(FavoriteType type, Long id) {
        favorites.findByUser_IdAndTargetTypeAndTargetId(users.currentUser().getId(), type, id).ifPresent(favorites::delete);
    }

    FavoriteType type(String value) {
        try {
            return FavoriteType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported favorite type");
        }
    }
}
