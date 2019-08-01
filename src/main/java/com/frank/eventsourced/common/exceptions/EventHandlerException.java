package com.frank.eventsourced.common.exceptions;

/**
 * @author ftorriani
 */
public class EventHandlerException extends RuntimeException {

    public EventHandlerException( String message ) {
        super( message );
    }

    public EventHandlerException( String message, Throwable cause ) {
        super( message, cause );
    }
}
