package com.frank.eventsourced.app.schema;

import com.frank.eventsourced.common.topics.TopicSerDe;
import com.frank.eventsourced.model.app.App;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * A utility class that represents topics
 */
@Log4j2
@Component
public class Schema {

    private final String schemaRegistryUrl;

    private TopicSerDe<String, SpecificRecord> eventLog;
    private TopicSerDe<String, App> stateTopic;

    public Schema(@Value("${schema.registry.url}") String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @PostConstruct
    public void init() {
        eventLog = new TopicSerDe<>("app-events", Serdes.String(), new SpecificAvroSerde<>());
        eventLog.configureValueSerDe(this.schemaRegistryUrl);

        stateTopic = new TopicSerDe<>("app-state", Serdes.String(), new SpecificAvroSerde<>());
        stateTopic.configureValueSerDe(this.schemaRegistryUrl);
    }

    public TopicSerDe<String, SpecificRecord> eventLogTopic() {
        return eventLog;
    }

    public TopicSerDe<String, App> stateTopic() {
        return stateTopic;
    }
}