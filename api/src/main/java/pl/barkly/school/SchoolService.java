package pl.barkly.school;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.school.api.SchoolCreateRequest;
import pl.barkly.school.api.SchoolResponse;
import pl.barkly.school.api.SchoolImageResponse;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserRole;
import pl.barkly.user.UserService;

import java.text.Normalizer;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pl.barkly.query.PageResponse;
import pl.barkly.favorite.FavoriteService;
import pl.barkly.favorite.FavoriteType;

@Service
public class SchoolService {
    private final SchoolRepository schools;
    private final UserService users;
    private final SchoolImageRepository images;
    private final FavoriteService favorites;

    public SchoolService(SchoolRepository schools, UserService users, SchoolImageRepository images, FavoriteService favorites) {
        this.schools = schools;
        this.users = users;
        this.images = images;
        this.favorites = favorites;
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> findAll() {
        return schools.findAllByOrderByNameAsc().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<SchoolResponse> search(String query, int page, int size, boolean favoritesOnly) {
        var pageable = pageRequest(page, size);
        if (!favoritesOnly) return PageResponse.from(schools.search(normalizeQuery(query), pageable), this::response);
        var ids = favorites.ids(FavoriteType.SCHOOL);
        if (ids.isEmpty()) return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        return PageResponse.from(schools.searchFavorites(normalizeQuery(query), ids, pageable), this::response);
    }

    @Transactional(readOnly = true)
    public List<SchoolResponse> findMine() {
        return schools.findAllByOwnerIdOrderByNameAsc(users.currentUser().getId()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public SchoolResponse findBySlug(String slug) {
        SchoolEntity school = findEntityBySlug(slug);
        return response(school);
    }

    @Transactional
    public SchoolResponse create(SchoolCreateRequest request) {
        UserEntity owner = users.currentUser();
        if (owner.getRole() == UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school administrators can create schools");
        }
        SchoolEntity school = schools.saveAndFlush(new SchoolEntity(request, owner));
        school.setGeneratedSlug(slugBase(school.getName()) + "-" + school.getId());
        return response(school);
    }

    @Transactional
    public SchoolResponse update(Long id, SchoolCreateRequest request) {
        SchoolEntity school = findEntityWithOwner(id);
        requireManagementAccess(school);
        school.update(request);
        return response(school);
    }

    @Transactional(readOnly = true)
    public SchoolEntity findEntityWithOwner(Long id) {
        return schools.findWithOwnerById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
    }

    @Transactional(readOnly = true)
    public SchoolEntity findEntityBySlug(String slug) {
        return schools.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
    }

    public void requireManagementAccess(SchoolEntity school) {
        UserEntity current = users.currentUser();
        if (current.getRole() == UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only school administrators can manage schools");
        }
        if (current.getRole() == UserRole.SUPER_ADMIN || school.getOwner().getId().equals(current.getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this school");
    }

    @Transactional
    public SchoolImageResponse addImage(Long schoolId, org.springframework.web.multipart.MultipartFile image,
                                        pl.barkly.training.image.TrainingImageStorage storage) {
        SchoolEntity school = findEntityWithOwner(schoolId);
        requireManagementAccess(school);
        String key = storage.store(image);
        try {
            return SchoolImageResponse.from(images.save(new SchoolImageEntity(school, key)));
        } catch (RuntimeException exception) {
            storage.delete(key);
            throw exception;
        }
    }

    @Transactional
    public void deleteImage(Long imageId, pl.barkly.training.image.TrainingImageStorage storage) {
        SchoolImageEntity image = images.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School image not found"));
        SchoolEntity school = findEntityWithOwner(image.getSchoolId());
        requireManagementAccess(school);
        images.delete(image);
        storage.delete(image.getImageKey());
    }

    private SchoolResponse response(SchoolEntity school) {
        var gallery = images.findAllBySchool_IdOrderByIdAsc(school.getId()).stream().map(SchoolImageResponse::from).toList();
        return new SchoolResponse(school.getId(), school.getName(), school.getSlug(), school.getAddress(), school.getKrs(),
                school.getDescription(), school.getActivities(), school.getPricing(), gallery);
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 50), Sort.by("name").ascending());
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private String slugBase(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return normalized.isBlank() ? "szkola" : normalized;
    }
}
