package be.kdg.int5.domain;

import java.util.Objects;

public record Rule(int stepNumber, String rule) {
    public Rule {
        Objects.requireNonNull(rule);
    }

    public String toJson() {
        return "{\"stepNumber\": "+stepNumber()+", \"rule\": \""+rule()+"\"}";
    }
}
