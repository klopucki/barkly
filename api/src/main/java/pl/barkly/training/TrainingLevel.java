package pl.barkly.training;

enum TrainingLevel {

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