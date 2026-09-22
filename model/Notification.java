package com.iwish.client.model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String message;
    private final LocalDateTime time;

    public Notification(String message) {
        this.message = message;
        this.time = LocalDateTime.now();
    }
    public Notification(String message, LocalDateTime time) {
        this.message = message;
        this.time = time;
    }
    public String getMessage() {
        return message;
    }
    @Override
    public String toString() {
        return "[" + time.format(FORMAT) + "] " + message;
    }
}
