package pl.barkly.training;

import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;

import java.util.List;
import pl.barkly.query.PageResponse;

public interface TrainingFacade {
    List<TrainingResponse> findTrainings();

    PageResponse<TrainingResponse> searchTrainings(String query, String type, int page, int size, boolean favoritesOnly);

    TrainingResponse findTraining(Long id);

    TrainingResponse createTraining(TrainingCreateRequest request);

    TrainingResponse updateTraining(Long id, TrainingCreateRequest request);

    BookingResponse bookTraining(Long trainingId, BookingCreateRequest request);

    BookingResponse quickBookTraining(Long trainingId, Long dogId);

    List<BookingResponse> findBookings(Long trainingId);

    void deleteTraining(Long id);

    void deleteBooking(Long id);

    TrainingResponse togglePaw(Long id);
}
