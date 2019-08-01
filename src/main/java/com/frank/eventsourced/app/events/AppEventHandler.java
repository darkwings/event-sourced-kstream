package com.frank.eventsourced.app.events;

import com.frank.eventsourced.common.events.EventHandler;
import com.frank.eventsourced.common.exceptions.EventHandlerException;
import com.frank.eventsourced.events.platform.app.AppCreated;
import com.frank.eventsourced.events.platform.app.WidgetAdded;
import com.frank.eventsourced.model.app.App;
import com.frank.eventsourced.model.app.Widget;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author ftorriani
 */
@Slf4j
@Component
@Qualifier("appEvent")
public class AppEventHandler implements EventHandler<App> {

    public App apply( SpecificRecord event, App currentState ) {

        try {
            if ( event.getClass().isAssignableFrom( AppCreated.class ) ) {
                AppCreated appCreated = (AppCreated) event;
                log.info( "Applying creation event '{}' [{}]",
                        appCreated.getClass().getName(),
                        appCreated );
                return App.newBuilder().
                        setKey( appCreated.getTenantId() + "|" + appCreated.getUserId() ).
                        setTenantId( appCreated.getTenantId() ).
                        setUserId( appCreated.getUserId() ).
                        setWidgets( new ArrayList<>() ).
                        setVersion( 0 ).
                        build();
            }
            else if ( event.getClass().isAssignableFrom( WidgetAdded.class ) ) {
                App updatedState = App.newBuilder( currentState ).build();

                WidgetAdded itemAdded = (WidgetAdded) event;
                log.info( "Applying event '{}' [{}] to app '{}'", event.getClass().getName(), itemAdded,
                        currentState.getKey() );
                Widget modelItem = Widget.newBuilder().
                        setWidgetId( itemAdded.getWidget().getWidgetId() ).
                        setData( itemAdded.getWidget().getData() ).
                        setMeta( itemAdded.getWidget().getMeta() ).
                        build();
                updatedState.getWidgets().add( modelItem );

                updateVersion( updatedState, currentState );
                
                return updatedState;
            }
            else {
                throw new EventHandlerException( "Unknown event " + event.getClass().getName() );
            }
        }
        catch ( EventHandlerException e ) {
            throw e;
        }
        catch ( Exception e ) {
            log.error( "Failed to handle event (payload : {}). Rethrowing exception as EventHandlerException",
                    event.getClass().getName() );
            throw new EventHandlerException( "Failed to handle event", e );
        }
    }

    private void updateVersion( App newState, App currentState ) {
        newState.setVersion( currentState.getVersion() + 1 );
    }
}
