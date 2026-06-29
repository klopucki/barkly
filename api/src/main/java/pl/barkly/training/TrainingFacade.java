package pl.barkly.training;

import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;

import java.util.List;

public interface TrainingFacade {
    List<TrainingResponse> findTrainings();

    TrainingResponse findTraining(Long id);

    TrainingResponse createTraining(TrainingCreateRequest request);

    BookingResponse bookTraining(Long trainingId, BookingCreateRequest request);

    List<BookingResponse> findBookings(Long trainingId);
}
