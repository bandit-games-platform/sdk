package be.kdg.int5.domain;

import java.util.Objects;

public record Achievement(int uniqueNumber, String title, String description, int counterTotal) {

    public Achievement(int uniqueNumber, String title, String description) {
        this(uniqueNumber, title, description, 1);
    }

    public Achievement {
        Objects.requireNonNull(title);
        Objects.requireNonNull(description);
    }

    public String toJson() {
        String json = "{\"uniqueNumber\": "+uniqueNumber()+", ";
        json += "\"title\": \""+title()+"\", ";
        json += "\"description\": \""+description()+"\", ";
        json +=  "\"counterTotal\": \""+counterTotal()+"\"}";
        return json;
    }
}
