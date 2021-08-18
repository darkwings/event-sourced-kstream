package com.frank.eventsourced.app.events;

import com.frank.eventsourced.common.events.EventHandler;
import com.frank.eventsourced.common.exceptions.EventHandlerException;
import com.frank.eventsourced.events.platform.app.AppCancelled;
import com.frank.eventsourced.events.platform.app.AppCreated;
import com.frank.eventsourced.events.platform.app.WidgetAdded;
import com.frank.eventsourced.model.app.App;
import com.frank.eventsourced.model.app.Widget;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * @author ftorriani
 */
@Log4j2
@Component
@Qualifier("appEvent")
public class AppEventHandler implements EventHandler<App> {

    private final Map<String, AppEventProcessor> eventProcessors;
    {
        eventProcessors = new HashMap<>();
        eventProcessors.put(AppCreated.class.getName(), new AppCreatedProcessor());
        eventProcessors.put(WidgetAdded.class.getName(), new WidgetAddedProcessor());
        eventProcessors.put(AppCancelled.class.getName(), new AppCancelledProcessor());
    }

    public App apply(SpecificRecord event, App currentState) {

        try {
            AppEventProcessor processor = ofNullable(eventProcessors.get(event.getClass().getName()))
                    .orElseThrow(() -> new EventHandlerException("Unknown event " + event.getClass().getName()));
            return processor.getNewState(event, currentState);

        } catch (EventHandlerException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to handle event (payload : {}). Rethrowing exception as EventHandlerException",
                    event.getClass().getName());
            throw new EventHandlerException("Failed to handle event", e);
        }
    }

    private static void updateVersion(App newState, App currentState) {
        newState.setVersion(currentState.getVersion() + 1);
    }

    public interface AppEventProcessor {

        App getNewState(SpecificRecord event, App currentState);
    }

    static class AppCreatedProcessor implements AppEventProcessor {
        @Override
        public App getNewState(SpecificRecord event, App currentState) {
            AppCreated appCreated = (AppCreated) event;
            log.info("Applying creation event '{}' [{}]",
                    appCreated.getClass().getName(),
                    appCreated);
            return App.newBuilder().
                    setKey(appCreated.getTenantId() + "|" + appCreated.getUserId()).
                    setTenantId(appCreated.getTenantId()).
                    setUserId(appCreated.getUserId()).
                    setWidgets(new ArrayList<>()).
                    setVersion(0).
                    build();
        }
    }

    static class WidgetAddedProcessor implements AppEventProcessor {
        @Override
        public App getNewState(SpecificRecord event, App currentState) {
            App updatedState = App.newBuilder(currentState).build();

            WidgetAdded itemAdded = (WidgetAdded) event;
            log.info("Applying event '{}' [{}] to app '{}'", event.getClass().getName(), itemAdded,
                    currentState.getKey());
            Widget modelItem = Widget.newBuilder().
                    setWidgetId(itemAdded.getWidget().getWidgetId()).
                    setData(itemAdded.getWidget().getData()).
                    setMeta(itemAdded.getWidget().getMeta()).
                    build();
            updatedState.getWidgets().add(modelItem);

            updateVersion(updatedState, currentState);

            return updatedState;
        }
    }

    static class AppCancelledProcessor implements AppEventProcessor {

        @Override
        public App getNewState(SpecificRecord event, App currentState) {
            log.info("Applying event '{}' to app '{}'", event.getClass().getName(),
                    currentState.getKey());

            // Return a tombstone
            return null;
        }
    }
}
