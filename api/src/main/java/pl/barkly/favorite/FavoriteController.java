package pl.barkly.favorite;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.barkly.favorite.api.FavoriteResponse;

import java.util.List;

@RestController
class FavoriteController {
    private final FavoriteService favorites;
    FavoriteController(FavoriteService favorites) {
        this.favorites = favorites;
    }

    @GetMapping("/api/favorites")
    List<FavoriteResponse> mine() {
        return favorites.mine();
    }
    @PutMapping("/api/favorites/{type}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void add(@PathVariable String type, @PathVariable Long id) {
        favorites.add(favorites.type(type), id);
    }
    @DeleteMapping("/api/favorites/{type}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@PathVariable String type, @PathVariable Long id) {
        favorites.remove(favorites.type(type), id);
    }
}
