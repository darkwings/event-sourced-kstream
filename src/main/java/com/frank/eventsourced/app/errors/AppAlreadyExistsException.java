package com.frank.eventsourced.app.errors;

/**
 * @author ftorriani
 */
public class AppAlreadyExistsException extends RuntimeException {

    public AppAlreadyExistsException(String message) {
        super(message);
    }

    public AppAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
