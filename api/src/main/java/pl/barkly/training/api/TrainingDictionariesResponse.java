package pl.barkly.training.api;

import java.util.List;

public record TrainingDictionariesResponse(
        List<DictionaryValueResponse> trainingTypes,
        List<TrainingLevelDictionaryResponse> trainingLevels,
        List<DictionaryValueResponse> targetGroups
) {
}
