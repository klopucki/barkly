package pl.barkly.training.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrainingResponse(
        Long id,
        Long schoolId,
        String title,
        String trainerName,
        LocalDateTime startAt,
        Integer capacity,
        int bookedCount,
        String imageKey,
        long pawCount,
        boolean pawedByCurrentUser,
        String description,
        String eventKind,
        String priceMode,
        BigDecimal price,
        String locationAddress,
        Double latitude,
        Double longitude) {}
