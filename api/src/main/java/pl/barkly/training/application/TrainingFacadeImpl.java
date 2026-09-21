package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import pl.barkly.training.TrainingFacade;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;

import java.util.List;
import pl.barkly.query.PageResponse;

@Service
class TrainingFacadeImpl implements TrainingFacade {

    private final TrainingCommandService trainingCommandService;
    private final TrainingQueryService trainingQueryService;
    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;
    private final TrainingPawReactionService pawReactionService;

    TrainingFacadeImpl(
            TrainingCommandService trainingCommandService,
            TrainingQueryService trainingQueryService,
            BookingCommandService bookingCommandService,
            BookingQueryService bookingQueryService, TrainingPawReactionService pawReactionService
    ) {
        this.trainingCommandService = trainingCommandService;
        this.trainingQueryService = trainingQueryService;
        this.bookingCommandService = bookingCommandService;
        this.bookingQueryService = bookingQueryService;
        this.pawReactionService = pawReactionService;
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
    public PageResponse<TrainingResponse> searchTrainings(String query, String type, int page, int size, boolean favoritesOnly) {
        return trainingQueryService.search(query, type, page, size, favoritesOnly);
    }

    @Override
    public TrainingResponse updateTraining(Long id, TrainingCreateRequest request) {
        return trainingCommandService.update(id, request);
    }

    @Override
    public BookingResponse bookTraining(Long trainingId, BookingCreateRequest request) {
        return bookingCommandService.book(trainingId, request);
    }

    @Override
    public List<BookingResponse> findBookings(Long trainingId) {
        return bookingQueryService.findByTrainingId(trainingId);
    }

    @Override
    public void deleteTraining(Long id) {
        trainingCommandService.deleteTraining(id);
    }

    @Override
    public void deleteBooking(Long id) {
        bookingCommandService.deleteBooking(id);
    }

    @Override
    public BookingResponse quickBookTraining(Long trainingId, Long dogId) { return bookingCommandService.quickBook(trainingId, dogId); }

    @Override
    public TrainingResponse togglePaw(Long id) { return pawReactionService.toggle(id); }


}
