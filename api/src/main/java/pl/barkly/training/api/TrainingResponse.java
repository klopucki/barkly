package pl.barkly.training.api;

import java.time.LocalDateTime;

public record TrainingResponse(
        Long id,
        Long schoolId,
        String title,
        String trainerName,
        DictionaryValueResponse trainingType,
        DictionaryValueResponse trainingLevel,
        DictionaryValueResponse targetGroup,
        boolean homeVisit,
        LocalDateTime startAt,
        Integer capacity,
        int bookedCount,
        String imageKey
) {
}
