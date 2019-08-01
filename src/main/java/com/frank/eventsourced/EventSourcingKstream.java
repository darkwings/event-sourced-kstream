package com.frank.eventsourced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ftorriani
 */
@SpringBootApplication
public class EventSourcingKstream {

    public static void main( String[] args ) {

        SpringApplication.run( EventSourcingKstream.class, args );

    }
}
