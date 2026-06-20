package pl.barkly.training;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
class TrainingService {

    List<Training> findAll() {
        return List.of(
                new Training(
                        1L,
                        "Podstawy posłuszeństwa",
                        "Trening dla początkujących psów i przewodników.",
                        TrainingLevel.BEGINNER,
                        LocalDate.now().plusDays(7),
                        10
                ),
                new Training(
                        2L,
                        "Socjalizacja w grupie",
                        "Zajęcia dla psów uczących się pracy w rozproszeniach.",
                        TrainingLevel.INTERMEDIATE,
                        LocalDate.now().plusDays(14),
                        8
                )
        );
    }
}