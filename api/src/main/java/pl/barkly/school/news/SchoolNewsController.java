package pl.barkly.school.news;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.barkly.school.SchoolService;
import pl.barkly.school.news.api.*;
import pl.barkly.user.UserService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import pl.barkly.query.PageResponse;
import pl.barkly.favorite.FavoriteService;
import pl.barkly.favorite.FavoriteType;

@RestController
class SchoolNewsController {
    private final SchoolNewsRepository news;
    private final SchoolService schools;
    private final UserService users;
    private final FavoriteService favorites;

    SchoolNewsController(SchoolNewsRepository news, SchoolService schools, UserService users, FavoriteService favorites) {
        this.news = news;
        this.schools = schools;
        this.users = users;
        this.favorites = favorites;
    }

    @GetMapping("/api/news")
    List<SchoolNewsResponse> all() {
        return news.findAllByOrderByPublishedAtDesc().stream().map(SchoolNewsResponse::from).toList();
    }

    @GetMapping("/api/news/{id}")
    SchoolNewsResponse byId(@PathVariable Long id) {
        var item = get(id);
        if (!item.isActive()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "News not found");
        }
        return SchoolNewsResponse.from(item);
    }

    @GetMapping("/api/query/news")
    PageResponse<SchoolNewsResponse> search(@RequestParam(defaultValue = "") String query,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = "false") boolean favoritesOnly) {
        var pageable = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 50));
        var ids = favoritesOnly ? favorites.ids(FavoriteType.ARTICLE) : List.<Long>of();
        if (favoritesOnly && ids.isEmpty()) return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        var result = favoritesOnly ? news.searchFavorites(query.trim(), ids, pageable) : news.search(query.trim(), pageable);
        return PageResponse.from(result, SchoolNewsResponse::from);
    }

    @GetMapping("/api/schools/{schoolId}/news")
    List<SchoolNewsResponse> bySchool(@PathVariable Long schoolId) {
        return news.findAllBySchoolIdOrderByPublishedAtDesc(schoolId).stream()
                .map(SchoolNewsResponse::from).toList();
    }

    @GetMapping("/api/my/schools/{schoolId}/news")
    List<SchoolNewsResponse> managedBySchool(@PathVariable Long schoolId) {
        var school = schools.findEntityWithOwner(schoolId);
        schools.requireManagementAccess(school);
        return news.findAllManagedBySchoolIdOrderByPublishedAtDesc(schoolId).stream()
                .map(SchoolNewsResponse::from).toList();
    }

    @PostMapping("/api/schools/{schoolId}/news")
    SchoolNewsResponse create(@PathVariable Long schoolId, @Valid @RequestBody SchoolNewsRequest request) {
        var school = schools.findEntityWithOwner(schoolId);
        schools.requireManagementAccess(school);
        return SchoolNewsResponse.from(news.save(new SchoolNewsEntity(school, users.currentUser(), request)));
    }

    @PutMapping("/api/news/{id}")
    SchoolNewsResponse update(@PathVariable Long id, @Valid @RequestBody SchoolNewsRequest request) {
        var item = get(id);
        schools.requireManagementAccess(schools.findEntityWithOwner(item.getSchoolId()));
        item.update(request);
        news.save(item);
        return SchoolNewsResponse.from(item);
    }

    @DeleteMapping("/api/news/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        var item = get(id);
        schools.requireManagementAccess(schools.findEntityWithOwner(item.getSchoolId()));
        news.delete(item);
    }

    private SchoolNewsEntity get(Long id) {
        return news.findWithDetailsById(id).orElseThrow(
                () -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));
    }
}
