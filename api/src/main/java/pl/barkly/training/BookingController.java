package pl.barkly.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.barkly.training.api.BookingCreateRequest;
import pl.barkly.training.api.BookingResponse;

import java.util.List;

@RestController
class BookingController {
    private final TrainingFacade trainingFacade;

    BookingController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @PostMapping("/api/trainings/{trainingId}/bookings")
    BookingResponse book(
            @PathVariable Long trainingId,
            @RequestBody BookingCreateRequest request
    ) {
        return trainingFacade.bookTraining(trainingId, request);
    }

    @GetMapping("/api/trainings/{trainingId}/bookings")
    List<BookingResponse> findBookings(@PathVariable Long trainingId) {
        return trainingFacade.findBookings(trainingId);
    }
}
