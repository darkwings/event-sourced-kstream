package com.frank.eventsourced.app.commands.handler;

import com.frank.eventsourced.app.commands.beans.AddWidgetCommand;
import com.frank.eventsourced.app.commands.beans.CancelAppCommand;
import com.frank.eventsourced.app.commands.beans.CreateAppCommand;
import com.frank.eventsourced.commands.platform.app.AddWidget;
import com.frank.eventsourced.commands.platform.app.CancelApp;
import com.frank.eventsourced.commands.platform.app.CommandFailure;
import com.frank.eventsourced.commands.platform.app.CreateApp;
import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.commands.handler.CommandHandler;
import com.frank.eventsourced.common.exceptions.CommandException;
import com.frank.eventsourced.common.utils.MessageUtils;
import com.frank.eventsourced.events.platform.app.AppCancelled;
import com.frank.eventsourced.events.platform.app.AppCreated;
import com.frank.eventsourced.events.platform.app.Widget;
import com.frank.eventsourced.events.platform.app.WidgetAdded;
import com.frank.eventsourced.model.app.App;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.frank.eventsourced.common.exceptions.CommandError.*;
import static com.frank.eventsourced.common.utils.MessageUtils.*;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * @author ftorriani
 */
@Component
@Qualifier("appCommand")
@Log4j2
public class AppCommandHandler implements CommandHandler<App> {

    private final Map<String, AppCommandProcessor> processors;

    public AppCommandHandler() {
        processors = new HashMap<>();
        processors.put(CreateApp.class.getName(), new CreateAppCommandProcessor());
        processors.put(AddWidget.class.getName(), new AddWidgetCommandProcessor());
        processors.put(CancelApp.class.getName(), new CancelAppCommandProcessor());
    }

    public Optional<SpecificRecord> apply(SpecificRecord command, App currentState) throws CommandException {

        try {
            AppCommandProcessor commandProcessor = ofNullable(processors.get(command.getClass().getName()))
                    .orElseThrow(() -> new CommandException(format("Command %s is unknown", command.getClass().getName())));
            return commandProcessor.process(command, currentState);
        }
        catch (Exception e) {
            log.error(format("Failed to process command %s on app %s", command.getClass().getName(), currentState.getKey()), e);
            return Optional.of(generateFailure(command, e.getMessage()));
        }
    }

    public interface AppCommandProcessor {

        Optional<SpecificRecord> process(SpecificRecord command, App currentState);

        default void checkVersion(String aggregateId, int currentStateVersion, int commandVersion) {
            if (currentStateVersion != commandVersion) {
                log.error("App '{}' - command version mismatch detected. Throwing CommandException", aggregateId);
                throw new CommandException("Version mismatch: command version '" +
                        commandVersion + "', " +
                        "actual state version: '" + currentStateVersion + "'",
                        AGGREGATE_VERSION_CONFLICT);
            }
        }
    }

    @Log4j2
    public static class AddWidgetCommandProcessor implements AppCommandProcessor {

        @Override
        public Optional<SpecificRecord> process(SpecificRecord command, App currentState) {
            AddWidget addWidgetCommand = (AddWidget) command;
            if (currentState != null) {
                log.info("Processing command {} on aggregate {}", command.getClass().getSimpleName(),
                        currentState.getKey());

                checkVersion(addWidgetCommand.getKey(), currentState.getVersion(), addWidgetCommand.getVersion());

                Widget item = Widget.newBuilder().
                        setWidgetId(addWidgetCommand.getWidget().getWidgetId()).
                        setMeta(addWidgetCommand.getWidget().getMeta()).
                        setData(addWidgetCommand.getWidget().getData()).build();

                // Check if an item already exists with the given item
                if (currentState.getWidgets().stream().
                        anyMatch(i -> i.getWidgetId().equals(item.getWidgetId()))) {
                    log.error("App '{}' - widget with ID '{}' already exists. Throwing CommandException", addWidgetCommand.getKey(),
                            item.getWidgetId());
                    throw new CommandException("Widget with ID '" + item.getWidgetId() + "' already exists " +
                            "in app " + currentState.getKey(), GENERIC_ID_CONFLICT);
                }

                WidgetAdded payload = WidgetAdded.newBuilder().
                        setKey(addWidgetCommand.getKey()).
                        setEventId(UUID.randomUUID().toString()).  // TODO
                                setOperationId(UUID.randomUUID().toString()).  // TODO
                                setTenantId(addWidgetCommand.getTenantId()).
                        setUserId(addWidgetCommand.getUserId()).
                        setTimestampMs(Clock.systemUTC().millis()).
                        setWidget(item).build();

                return Optional.of(payload);
            } else {
                throw new CommandException("Unknown app '" + addWidgetCommand.getKey() + "'",
                        NOT_EXISTING_AGGREGATE);
            }
        }
    }

    @Log4j2
    public static class CreateAppCommandProcessor implements AppCommandProcessor {

        @Override
        public Optional<SpecificRecord> process(SpecificRecord command, App currentState) {
            CreateApp createCommand = (CreateApp) command;
            if (currentState == null) {
                log.info("Processing command {} to create a new aggregate", command.getClass().getSimpleName());
                AppCreated payload = AppCreated.newBuilder().
                        setTenantId(createCommand.getTenantId()).
                        setUserId(createCommand.getUserId()).
                        setTimestampMs(Clock.systemUTC().millis()).
                        setEventId(UUID.randomUUID().toString()). // TODO
                                setOperationId(UUID.randomUUID().toString()).  // TODO
                                setKey(createCommand.getKey()).build();
                return Optional.of(payload);
            } else {
                log.error("App '{}' already exists. It cannot be created. Throwing CommandException",
                        createCommand.getKey());
                throw new CommandException("App '" + createCommand.getKey() + "' already exists. It cannot be created",
                        ALREADY_EXISTING_AGGREGATE);
            }
        }
    }

    @Log4j2
    static class CancelAppCommandProcessor implements AppCommandProcessor {

        @Override
        public Optional<SpecificRecord> process(SpecificRecord command, App currentState) {
            CancelApp cancelAppCommand = (CancelApp) command;
            if (currentState == null) {
                throw new CommandException("Unknown app '" + cancelAppCommand.getKey() + "'",
                        NOT_EXISTING_AGGREGATE);
            }

            checkVersion(cancelAppCommand.getKey(), currentState.getVersion(), cancelAppCommand.getVersion());

            AppCancelled appCancelled = AppCancelled.newBuilder()
                    .setEventId(UUID.randomUUID().toString())
                    .setOperationId(UUID.randomUUID().toString())
                    .setTenantId(cancelAppCommand.getTenantId())
                    .setTimestampMs(Clock.systemUTC().millis())
                    .setUserId(cancelAppCommand.getUserId())
                    .build();

            return Optional.of(appCancelled);
        }
    }
}
