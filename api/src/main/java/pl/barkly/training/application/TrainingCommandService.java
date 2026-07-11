package pl.barkly.training.application;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.training.api.TrainingCreateRequest;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingEntity;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.training.persistence.TargetGroupEntity;
import pl.barkly.training.persistence.TargetGroupRepository;
import pl.barkly.training.persistence.TrainingLevelEntity;
import pl.barkly.training.persistence.TrainingLevelRepository;
import pl.barkly.training.persistence.TrainingTypeEntity;
import pl.barkly.training.persistence.TrainingTypeRepository;
import pl.barkly.training.exceptions.InvalidTrainingConfigurationException;
import pl.barkly.training.image.TrainingImageStorage;

import java.time.LocalDateTime;

@Service
class TrainingCommandService {

    private final TrainingRepository trainingRepository;
    private final BookingRepository bookingRepository;
    private final TrainingImageStorage imageStorage;
    private final TrainingTypeRepository typeRepository;
    private final TrainingLevelRepository levelRepository;
    private final TargetGroupRepository targetGroupRepository;

    TrainingCommandService(
            TrainingRepository trainingRepository,
            BookingRepository bookingRepository,
            TrainingImageStorage imageStorage,
            TrainingTypeRepository typeRepository,
            TrainingLevelRepository levelRepository,
            TargetGroupRepository targetGroupRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.bookingRepository = bookingRepository;
        this.imageStorage = imageStorage;
        this.typeRepository = typeRepository;
        this.levelRepository = levelRepository;
        this.targetGroupRepository = targetGroupRepository;
    }

    @Transactional
    TrainingResponse create(TrainingCreateRequest request) {
        TrainingConfiguration configuration = resolveConfiguration(request);
        TrainingEntity saved = trainingRepository.save(new TrainingEntity(
                request, configuration.type(), configuration.level(), configuration.targetGroup()
        ));
        return saved.toResponse(0);
    }

    private TrainingConfiguration resolveConfiguration(TrainingCreateRequest request) {
        TrainingTypeEntity type = typeRepository.findById(request.trainingTypeId())
                .filter(TrainingTypeEntity::isActive)
                .orElseThrow(() -> invalid("Selected training type is unavailable"));

        TrainingLevelEntity level = request.trainingLevelId() == null
                ? null
                : levelRepository.findById(request.trainingLevelId())
                        .filter(TrainingLevelEntity::isActive)
                        .orElseThrow(() -> invalid("Selected training level is unavailable"));

        if (level != null && level.getTrainingType() != null
                && !level.getTrainingType().getId().equals(type.getId())) {
            throw invalid("Selected level does not belong to this training type");
        }
        if (request.homeVisit() && !"CONSULTATION".equals(type.getCode())) {
            throw invalid("Home visit is available only for individual consultations");
        }
        TargetGroupEntity targetGroup = resolveTargetGroup(request.targetGroupId());
        return new TrainingConfiguration(type, level, targetGroup);
    }

    @Transactional
    TrainingResponse update(Long id, TrainingCreateRequest request) {
        TrainingEntity training = trainingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        TrainingConfiguration configuration = resolveConfiguration(request);
        training.update(request, configuration.type(), configuration.level(), configuration.targetGroup());
        int bookedCount = bookingRepository.countByTrainingId(id);
        return training.toResponse(bookedCount);
    }

    private TargetGroupEntity resolveTargetGroup(Long targetGroupId) {
        if (targetGroupId == null) {
            return null;
        }
        return targetGroupRepository.findById(targetGroupId)
                .filter(TargetGroupEntity::isActive)
                .orElseThrow(() -> invalid("Selected target group is unavailable"));
    }

    private InvalidTrainingConfigurationException invalid(String message) {
        return new InvalidTrainingConfigurationException(message);
    }

    private record TrainingConfiguration(
            TrainingTypeEntity type,
            TrainingLevelEntity level,
            TargetGroupEntity targetGroup
    ) {
    }

    @Transactional
    public void deleteTraining(Long id) {
        TrainingEntity training = trainingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        LocalDateTime deletedAt = LocalDateTime.now();

        training.softDelete();
        bookingRepository.softDeleteByTrainingId(id, deletedAt);
        imageStorage.delete(training.getImageKey());
    }
}
