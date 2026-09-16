package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pl.barkly.query.PageResponse;
import pl.barkly.favorite.FavoriteService;
import pl.barkly.favorite.FavoriteType;

@Service
class TrainingQueryService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteService favorites;

    TrainingQueryService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository, FavoriteService favorites
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
        this.favorites = favorites;
    }

    List<TrainingResponse> findAll() {
        return trainingRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    PageResponse<TrainingResponse> search(String query, String type, int page, int size, boolean favoritesOnly) {
        var pageable = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 50), Sort.by("startAt").ascending());
        var ids = favoritesOnly ? favorites.ids(FavoriteType.TRAINING) : List.<Long>of();
        if (favoritesOnly && ids.isEmpty()) return new PageResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        var result = favoritesOnly ? trainingRepository.searchFavorites(normalize(query), normalize(type), ids, pageable) : trainingRepository.search(normalize(query), normalize(type), pageable);
        return PageResponse.from(result, this::toResponse);
    }

    TrainingResponse findById(Long id) {
        return trainingRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toResponse)
                .orElseThrow();
    }

    private TrainingResponse toResponse(TrainingEntity training) {
        int bookedCount = bookingRepository.countByTrainingId(training.getId());
        return training.toResponse(bookedCount);
    }

    private String normalize(String value) { return value == null ? "" : value.trim(); }
}
