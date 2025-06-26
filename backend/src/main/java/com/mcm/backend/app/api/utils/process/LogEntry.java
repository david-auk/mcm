package com.mcm.backend.app.api.utils.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class LogEntry {
    private Instant timestamp;
    private String message;

    public LogEntry() { /* for Jackson */ }

    public LogEntry(String message) {
        this.timestamp = Instant.now();
        this.message   = message;
    }

    // Jackson will happily serialize a `long`
    @JsonProperty("timestamp")
    public long getTimestampMillis() {
        return timestamp.toEpochMilli();
    }

    // If you still want the Instant field internally…
    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
