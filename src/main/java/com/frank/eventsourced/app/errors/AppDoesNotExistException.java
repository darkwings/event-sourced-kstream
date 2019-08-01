package com.frank.eventsourced.app.errors;

/**
 * @author ftorriani
 */
public class AppDoesNotExistException extends RuntimeException {

    public AppDoesNotExistException( String message ) {
        super( message );
    }

    public AppDoesNotExistException( String message, Throwable cause ) {
        super( message, cause );
    }
}
