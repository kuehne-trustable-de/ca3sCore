package de.trustable.ca3s.core.service.util;

import java.time.Instant;

public class NameMessages {
    private final String name;
    private final String message;
    private final Instant on;

    public NameMessages(String name, String message, Instant on) {
        this.name = name;
        this.message = message;
        this.on = on;
    }


    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public Instant getOn() {
        return on;
    }
}
