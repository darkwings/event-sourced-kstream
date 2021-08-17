package com.frank.eventsourced.common.eventsourcing;

/**
 * @author ftorriani
 */
public class RemoteStateException extends RuntimeException {

    public RemoteStateException(String message) {
        super(message);
    }

    public RemoteStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
