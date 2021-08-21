package com.frank.eventsourced.common.commands.handler;

import com.frank.eventsourced.common.exceptions.CommandException;
import org.apache.avro.specific.SpecificRecord;

import java.util.Optional;

/**
 * The command handler. It validates the command given the business rules of the domain
 *
 * @param <A> the aggregate
 */
public interface CommandHandler<A> {

    /**
     * @param command   the command
     * @param aggregate the current state
     * @return the event to be forwarded or a {@link com.frank.eventsourced.commands.platform.app.CommandFailure} object
     */
    Optional<SpecificRecord> apply(SpecificRecord command, A aggregate) throws CommandException;
}
