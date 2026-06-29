package pl.barkly.training;

public enum TrainingLevel {

    PUPPY("puppy"),
    BASIC("basic"),
    ADVANCED("advanced"),
    SPORT("sport");

    private final String code;

    TrainingLevel(String code) {
        this.code = code;
    }

    String code() {
        return code;
    }
}