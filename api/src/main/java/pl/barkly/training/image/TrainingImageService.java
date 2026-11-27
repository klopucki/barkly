package pl.barkly.training.image;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pl.barkly.school.SchoolImageRepository;
import pl.barkly.school.SchoolService;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.exceptions.ResourceNotFoundException;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;

@Service
public class TrainingImageService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;
    private final TrainingImageStorage imageStorage;
    private final SchoolService schoolService;
    private final SchoolImageRepository schoolImages;

    TrainingImageService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository,
            TrainingImageStorage imageStorage,
            SchoolService schoolService,
            SchoolImageRepository schoolImages) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
        this.imageStorage = imageStorage;
        this.schoolService = schoolService;
        this.schoolImages = schoolImages;
    }

    @Transactional
    public TrainingResponse replace(Long trainingId, MultipartFile image) {
        TrainingEntity training =
                trainingRepository
                        .findByIdAndDeletedAtIsNull(trainingId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Training %d not found".formatted(trainingId)));
        schoolService.requireManagementAccess(
                schoolService.findEntityWithOwner(training.toResponse().schoolId()));

        String previousKey = training.getImageKey();
        String newKey = imageStorage.store(image);
        try {
            training.setImageKey(newKey);
            trainingRepository.saveAndFlush(training);
        } catch (RuntimeException exception) {
            imageStorage.delete(newKey);
            throw exception;
        }

        imageStorage.delete(previousKey);
        int bookedCount = bookingRepository.countByTrainingIdAndDeletedAtIsNull(trainingId);
        return training.toResponse(bookedCount);
    }

    @Transactional(readOnly = true)
    public Resource load(String key) {
        if (!trainingRepository.existsByImageKeyAndDeletedAtIsNull(key)
                && !schoolImages.existsByImageKey(key)) {
            throw new ResourceNotFoundException("Image not found");
        }
        return imageStorage.load(key);
    }
}
