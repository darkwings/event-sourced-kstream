package com.frank.eventsourced.app.topics;

import com.frank.eventsourced.commands.platform.app.CommandFailure;
import com.frank.eventsourced.common.topics.Topics;
import com.frank.eventsourced.common.topics.TopicSerDe;
import com.frank.eventsourced.model.app.App;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * A utility class that represents topics
 */
@Log4j2
@Component
@Qualifier("appTopics")
public class AppTopics implements Topics<App> {

    private final String schemaRegistryUrl;

    private TopicSerDe<String, SpecificRecord> eventLog;
    private TopicSerDe<String, SpecificRecord> commandTopic;
    private TopicSerDe<String, App> stateTopic;
    private TopicSerDe<String, CommandFailure> failureTopic;

    public AppTopics(@Value("${schema.registry.url}") String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @PostConstruct
    public void init() {
        eventLog = new TopicSerDe<>("app-events", Serdes.String(), new SpecificAvroSerde<>());
        eventLog.configureValueSerDeAndSchemaNaming(this.schemaRegistryUrl, TopicRecordNameStrategy.class.getName());

        stateTopic = new TopicSerDe<>("app-state", Serdes.String(), new SpecificAvroSerde<>());
        stateTopic.configureValueSerDe(this.schemaRegistryUrl);

        commandTopic = new TopicSerDe<>("app-commands", Serdes.String(), new SpecificAvroSerde<>());
        commandTopic.configureValueSerDe(this.schemaRegistryUrl);

        failureTopic = new TopicSerDe<>("app-command-failures", Serdes.String(), new SpecificAvroSerde<>());
        failureTopic.configureValueSerDe(this.schemaRegistryUrl);
    }

    public TopicSerDe<String, SpecificRecord> eventLogTopic() {
        return eventLog;
    }

    public TopicSerDe<String, App> stateTopic() {
        return stateTopic;
    }

    @Override
    public TopicSerDe<String, SpecificRecord> commandTopic() {
        return commandTopic;
    }

    @Override
    public TopicSerDe<String, CommandFailure> commandFailureTopic() {
        return failureTopic;
    }
}