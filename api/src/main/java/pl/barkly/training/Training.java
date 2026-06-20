package pl.barkly.training;

import java.time.LocalDate;

record Training(
        Long id,
        String title,
        String description,
        TrainingLevel level,
        LocalDate date,
        int capacity
) {
}