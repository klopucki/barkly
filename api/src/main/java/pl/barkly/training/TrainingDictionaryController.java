package pl.barkly.training;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barkly.training.api.DictionaryValueResponse;
import pl.barkly.training.api.TrainingDictionariesResponse;
import pl.barkly.training.api.TrainingLevelDictionaryResponse;
import pl.barkly.training.persistence.TargetGroupRepository;
import pl.barkly.training.persistence.TrainingLevelRepository;
import pl.barkly.training.persistence.TrainingTypeRepository;

@RestController
class TrainingDictionaryController {

    private final TrainingTypeRepository typeRepository;
    private final TrainingLevelRepository levelRepository;
    private final TargetGroupRepository targetGroupRepository;

    TrainingDictionaryController(
            TrainingTypeRepository typeRepository,
            TrainingLevelRepository levelRepository,
            TargetGroupRepository targetGroupRepository
    ) {
        this.typeRepository = typeRepository;
        this.levelRepository = levelRepository;
        this.targetGroupRepository = targetGroupRepository;
    }

    @GetMapping("/api/training-dictionaries")
    TrainingDictionariesResponse findAll() {
        var types = typeRepository.findAllByActiveTrueOrderByName().stream()
                .map(value -> new DictionaryValueResponse(value.getId(), value.getCode(), value.getName()))
                .toList();
        var levels = levelRepository.findAllByActiveTrueOrderByName().stream()
                .map(value -> new TrainingLevelDictionaryResponse(
                        value.getId(),
                        value.getCode(),
                        value.getName(),
                        value.getTrainingType() == null ? null : value.getTrainingType().getId()
                ))
                .toList();
        var groups = targetGroupRepository.findAllByActiveTrueOrderByName().stream()
                .map(value -> new DictionaryValueResponse(value.getId(), value.getCode(), value.getName()))
                .toList();
        return new TrainingDictionariesResponse(types, levels, groups);
    }
}
