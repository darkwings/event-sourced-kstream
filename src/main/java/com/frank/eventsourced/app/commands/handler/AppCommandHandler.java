package com.frank.eventsourced.app.commands.handler;

import com.frank.eventsourced.app.commands.beans.AddWidgetCommand;
import com.frank.eventsourced.app.commands.beans.CreateAppCommand;
import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.commands.handler.CommandHandler;
import com.frank.eventsourced.common.exceptions.CommandException;
import com.frank.eventsourced.events.platform.app.AppCreated;
import com.frank.eventsourced.events.platform.app.Widget;
import com.frank.eventsourced.events.platform.app.WidgetAdded;
import com.frank.eventsourced.model.app.App;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static com.frank.eventsourced.common.exceptions.CommandError.*;

/**
 * @author ftorriani
 */
@Component
@Qualifier("appCommand")
@Log4j2
public class AppCommandHandler implements CommandHandler<App> {

    public Optional<SpecificRecord> apply(Command command, App currentState) throws CommandException {

        // TODO check da fare sempre nella superclasse

        try {
            if (command.getClass().isAssignableFrom(CreateAppCommand.class)) {
                if (currentState == null) {
                    log.info("Processing command {} to create a new aggregate", command.getClass().getSimpleName());
                    CreateAppCommand createCommand = (CreateAppCommand) command;
                    AppCreated payload = AppCreated.newBuilder().
                            setTenantId(createCommand.getTenantId()).
                            setUserId(createCommand.getUserId()).
                            setTimestampMs(Clock.systemUTC().millis()).
                            setEventId(UUID.randomUUID().toString()). // TODO
                                    setOperationId(UUID.randomUUID().toString()).  // TODO
                                    setKey(createCommand.aggregateId()).build();

                    return Optional.of(payload);
                } else {
                    log.error("App '{}' already exists. It cannot be created. Throwing CommandException",
                            command.aggregateId());
                    throw new CommandException("App '" + command.aggregateId() + "' already exists. It cannot be created",
                            ALREADY_EXISTING_AGGREGATE);
                }
            } else if (command.getClass().isAssignableFrom(AddWidgetCommand.class)) {

                if (currentState != null) {
                    log.info("Processing command {} on aggregate {}", command.getClass().getSimpleName(),
                            currentState.getKey());

                    if (currentState.getVersion() != command.expectedVersion()) {
                        log.error("App '{}' - command version mismatch detected. Throwing CommandException", command.aggregateId());
                        throw new CommandException("Version mismatch: command version '" +
                                command.expectedVersion() + "', " +
                                "actual state version: '" + currentState.getVersion() + "'",
                                AGGREGATE_VERSION_CONFLICT);
                    }

                    AddWidgetCommand addWidgetCommand = (AddWidgetCommand) command;

                    Widget item = Widget.newBuilder().
                            setWidgetId(addWidgetCommand.getWidgetId()).
                            setMeta(addWidgetCommand.getMeta()).
                            setData(addWidgetCommand.getData()).build();

                    // Check if an item already exists with the given item
                    if (currentState.getWidgets().stream().
                            anyMatch(i -> i.getWidgetId().equals(item.getWidgetId()))) {
                        log.error("App '{}' - widget with ID '{}' already exists. Throwing CommandException", command.aggregateId(),
                                item.getWidgetId());
                        throw new CommandException("Widget with ID '" + item.getWidgetId() + "' already exists " +
                                "in app " + currentState.getKey(), GENERIC_ID_CONFLICT);
                    }

                    WidgetAdded payload = WidgetAdded.newBuilder().
                            setKey(addWidgetCommand.aggregateId()).
                            setEventId(UUID.randomUUID().toString()).  // TODO
                                    setOperationId(UUID.randomUUID().toString()).  // TODO
                                    setTenantId(addWidgetCommand.getTenantId()).
                            setUserId(addWidgetCommand.getUserId()).
                            setTimestampMs(Clock.systemUTC().millis()).
                            setWidget(item).build();

                    return Optional.of(payload);
                } else {
                    throw new CommandException("Unknown app '" + command.aggregateId() + "'",
                            NOT_EXISTING_AGGREGATE);
                }
            } else {
                log.error("Unknown command {}", command.getClass().getSimpleName());
                throw new CommandException("Command " + command.getClass().getName() + " is unknown");
            }
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandException("Error handling command " + command.getClass().getName() + " on aggregate " +
                    command.aggregateId(), e);
        }
    }

}
