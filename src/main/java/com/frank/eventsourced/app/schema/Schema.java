package com.frank.eventsourced.app.schema;

import com.frank.eventsourced.common.topics.TopicSerDe;

import com.frank.eventsourced.model.app.App;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

/**
 * A utility class that represents topics
 * Could be part of the SDK
 */
@Slf4j
public class Schema {

    private String schemaRegistryUrl;
    private static final SpecificAvroSerde<App> APP_VIEW_VALUE_SERDE = new SpecificAvroSerde<>();

    public Schema( String schemaRegistryUrl ) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public static class Topics {

        public static final Map<String, TopicSerDe> ALL = new HashMap<>();
        public static TopicSerDe<String, SpecificRecord> APP_EVENTS;
        public static TopicSerDe<String, App> APP_VIEW;


        static {
            createTopics();
        }

        @SuppressWarnings( "unchecked" )
        private static void createTopics() {

            APP_EVENTS = new TopicSerDe("app-events", Serdes.String(), new SpecificAvroSerde() );
            APP_VIEW = new TopicSerDe<>( "app-state", Serdes.String(), APP_VIEW_VALUE_SERDE );
        }
    }

    public void configureSerdesWithSchemaRegistryUrl() {
        Topics.createTopics(); //wipe cached schema registry
        for ( TopicSerDe topic : Topics.ALL.values() ) {
            log.info( "Configuring schema registry for topic {}", topic.name() );
            configure( topic.keySerde(), schemaRegistryUrl );
            configure( topic.valueSerde(), schemaRegistryUrl );
        }
        configure( APP_VIEW_VALUE_SERDE, schemaRegistryUrl );
    }

    @SuppressWarnings( "unchecked" )
    private void configure( Serde serde, String url ) {
        if ( serde instanceof SpecificAvroSerde ) {
            log.info( "Configuring schema registry for value serde" );
            serde.configure( Collections.singletonMap( SCHEMA_REGISTRY_URL_CONFIG, url ), false );
        }
    }
}