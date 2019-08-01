package com.frank.eventsourced.common.errors;

import com.frank.eventsourced.common.commands.dispatcher.Result;
import com.frank.eventsourced.common.exceptions.CommandException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author ftorriani
 */
@ControllerAdvice
@Order(1)
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles exceptions of {@link com.frank.eventsourced.common.commands.dispatcher.CommandDispatcher}
     *
     * @param ex the exception
     * @param request the request
     * @return a {@link ResponseEntity} with error informations
     */
    @ExceptionHandler(value = { CommandException.class })
    protected ResponseEntity<Result> handleCommandException( RuntimeException ex, WebRequest request ) {
        CommandException commandException = (CommandException) ex;
        log.error( "Handling CommandException: {} ({} - {})",
                ex.getMessage(), commandException.httpStatus(), commandException.getCommandError() );
        return new ResponseEntity<>( new Result( commandException.getCommandError().name() ),
                commandException.httpStatus() );
    }
}
