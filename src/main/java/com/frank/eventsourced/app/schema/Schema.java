package com.frank.eventsourced.app.schema;

import com.frank.eventsourced.common.topics.TopicSerDe;
import com.frank.eventsourced.model.app.App;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.singletonMap;

/**
 * A utility class that represents topics
 */
@Log4j2
@Component
public class Schema {

    private final String schemaRegistryUrl;

    // private static final SpecificAvroSerde<App> APP_VIEW_VALUE_SERDE = new SpecificAvroSerde<>();
    private TopicSerDe<String, SpecificRecord> eventLog;
    private TopicSerDe<String, App> stateTopic;

    public Schema(@Value("${schema.registry.url}") String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @PostConstruct
    public void init() {
        eventLog = new TopicSerDe<>("app-events", Serdes.String(), new SpecificAvroSerde<>());
        stateTopic = new TopicSerDe<>("app-state", Serdes.String(), new SpecificAvroSerde<>());
        configure(eventLog.valueSerde(), this.schemaRegistryUrl);
        configure(stateTopic.valueSerde(), this.schemaRegistryUrl);
    }

    public TopicSerDe<String, SpecificRecord> eventLogTopic() {
        return eventLog;
    }

    public TopicSerDe<String, App> stateTopic() {
        return stateTopic;
    }

//    public static class Topics {
//
//        public static final Map<String, TopicSerDe> ALL = new HashMap<>();
//        public static TopicSerDe<String, SpecificRecord> APP_EVENTS;
//        public static TopicSerDe<String, App> APP_VIEW;
//
//
//        static {
//            createTopics();
//        }
//
//        private static void createTopics() {
//            APP_EVENTS = new TopicSerDe<>("app-events", Serdes.String(), new SpecificAvroSerde<>());
//            APP_VIEW = new TopicSerDe<>("app-state", Serdes.String(), APP_VIEW_VALUE_SERDE);
//            ALL.clear();
//            ALL.put("app-events", APP_EVENTS);
//            ALL.put("app-state", APP_VIEW);
//        }
//    }

//    @PostConstruct
//    public void configureSerDesWithSchemaRegistryUrl() {
//        Topics.createTopics(); //wipe cached schema registry
//        Topics.ALL.values().forEach(topic -> {
//            log.info("Configuring schema registry for topic {}", topic.name());
//            // configure(topic.keySerde(), schemaRegistryUrl);
//            configure(topic.valueSerde(), schemaRegistryUrl);
//        });
//        configure(APP_VIEW_VALUE_SERDE, schemaRegistryUrl);
//    }
//

    private <T> void configure(Serde<T> serde, String url) {
        serde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, url), false);
    }
}