package com.frank.eventsourced.common.exceptions;

/**
 * @author ftorriani
 */
public enum CommandError {

    GENERIC_ERROR,
    NOT_EXISTING_AGGREGATE,
    ALREADY_EXISTING_AGGREGATE,
    AGGREGATE_VERSION_CONFLICT,
    GENERIC_ID_CONFLICT;
}
