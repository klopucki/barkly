package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.TrainingFacade;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;

import java.util.List;

@Service
class TrainingFacadeImpl implements TrainingFacade {

    private final TrainingCommandService trainingCommandService;
    private final TrainingQueryService trainingQueryService;
    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;

    TrainingFacadeImpl(
            TrainingCommandService trainingCommandService,
            TrainingQueryService trainingQueryService,
            BookingCommandService bookingCommandService,
            BookingQueryService bookingQueryService
    ) {
        this.trainingCommandService = trainingCommandService;
        this.trainingQueryService = trainingQueryService;
        this.bookingCommandService = bookingCommandService;
        this.bookingQueryService = bookingQueryService;
    }

    @Override
    public List<TrainingResponse> findTrainings() {
        return trainingQueryService.findAll();
    }

    @Override
    public TrainingResponse findTraining(Long id) {
        return trainingQueryService.findById(id);
    }

    @Override
    public TrainingResponse createTraining(TrainingCreateRequest request) {
        return trainingCommandService.create(request);
    }

    @Override
    public BookingResponse bookTraining(Long trainingId, BookingCreateRequest request) {
        return bookingCommandService.book(trainingId, request);
    }

    @Override
    public List<BookingResponse> findBookings(Long trainingId) {
        return bookingQueryService.findByTrainingId(trainingId);
    }
}