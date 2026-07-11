package pl.barkly.training.api;

public record TrainingLevelDictionaryResponse(
        Long id,
        String code,
        String name,
        Long trainingTypeId
) {
}
