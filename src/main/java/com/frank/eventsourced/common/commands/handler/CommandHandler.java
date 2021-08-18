package com.frank.eventsourced.common.commands.handler;

import com.frank.eventsourced.common.commands.beans.Command;
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
     * @return the event
     * @throws CommandException if command is unknown
     */
    Optional<SpecificRecord> apply(Command command, A aggregate) throws CommandException;
}
