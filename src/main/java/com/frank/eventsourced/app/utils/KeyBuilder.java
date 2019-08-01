package com.frank.eventsourced.app.utils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author ftorriani
 */
public class KeyBuilder {

    public static String key( String tenantId, String userId ) {
        return tenantId + "|" + userId;
    }

    public static String[] split( String key ) {
        return key.split( "\\|" );
    }

    public static Optional<String> keyOf( Object event ) {
        try {
            Method m = event.getClass().getMethod( "getKey" );
            String key = (String) m.invoke( event );
            return Optional.of( key );
        }
        catch ( Exception e ) {
            try {
                Method m = event.getClass().getMethod( "aggregateId" );
                String key = (String) m.invoke( event );
                return Optional.of( key );
            }
            catch ( Exception e1 ) {
                return Optional.empty();
            }
        }
    }
}
