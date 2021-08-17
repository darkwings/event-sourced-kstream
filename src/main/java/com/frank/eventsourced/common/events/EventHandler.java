package com.frank.eventsourced.common.events;

import com.frank.eventsourced.common.exceptions.EventHandlerException;
import org.apache.avro.specific.SpecificRecord;

/**
 * The event handler object
 *
 * @param <S> the state to which the event will be applied
 */
public interface EventHandler<S extends SpecificRecord> {

    /**
     * Given the current state of an aggregate, it applies the event and return the updated state.
     * This method should not return a {@code null} value. In case of error, the method
     * should return the {@code currentState}
     *
     * @param event        the event
     * @param currentState the current state of the aggregate
     * @return the updated state
     * @throws EventHandlerException in case of error
     */
    S apply(SpecificRecord event, S currentState);
}
