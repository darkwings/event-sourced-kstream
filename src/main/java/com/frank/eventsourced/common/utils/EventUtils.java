package com.frank.eventsourced.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author ftorriani
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public abstract class EventUtils {

    public static <T extends SpecificRecord> String eventIdOf( T value ) {
        try {
            Method m = value.getClass().getMethod( "getEventId" );
            return (String) m.invoke( value );
        }
        catch ( Exception e ) {
            return "unknown";
        }
    }

    public static <T extends SpecificRecord> Optional<String> keyOf( T event ) {
        try {
            Method m = event.getClass().getMethod( "getKey" );
            String key = (String) m.invoke( event );
            return Optional.of( key );
        }
        catch ( Exception e ) {
            log.error("Failed to extract key", e);
            return Optional.empty();
        }
    }
}
