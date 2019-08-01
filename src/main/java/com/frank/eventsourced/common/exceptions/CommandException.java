package com.frank.eventsourced.common.exceptions;

import org.springframework.http.HttpStatus;

import static com.frank.eventsourced.common.exceptions.CommandError.*;

/**
 * @author ftorriani
 */
public class CommandException extends RuntimeException {

    private CommandError commandError;

    public CommandException( String message ) {
        super( message );
        this.commandError = CommandError.GENERIC_ERROR;
    }

    public CommandException( String message, Throwable cause ) {
        super( message, cause );
        this.commandError = CommandError.GENERIC_ERROR;
    }

    public CommandException( String message, CommandError commandError ) {
        super( message );
        this.commandError = commandError;
    }

//    public CommandException( String message, Throwable cause, CommandError commandError ) {
//        super( message, cause );
//        this.commandError = commandError;
//    }

    public CommandError getCommandError() {
        return commandError;
    }

    public HttpStatus httpStatus() {
        if ( commandError == ALREADY_EXISTING_AGGREGATE ||
                commandError == AGGREGATE_VERSION_CONFLICT ||
                commandError == GENERIC_ID_CONFLICT ) {
            return HttpStatus.CONFLICT;
        }
        else if ( commandError == NOT_EXISTING_AGGREGATE ) {
            return HttpStatus.NOT_FOUND;
        }
        else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
