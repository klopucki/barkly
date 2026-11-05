package pl.barkly.training.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import pl.barkly.training.api.TrainingResponse;

class TrainingDetailsAssertions {
    static void notificationDecision(boolean actual, boolean expected) {
        assertThat(actual).isEqualTo(expected);
    }

    static void paidDetailsPreserved(TrainingResponse response) {
        assertThat(response.price()).isEqualByComparingTo("80.50");
        assertThat(response.description()).isEqualTo("Opis");
    }

    static void priceCleared(TrainingResponse response) {
        assertThat(response.price()).isNull();
        assertThat(response.priceMode()).isEqualTo("TO_BE_AGREED");
    }
}
