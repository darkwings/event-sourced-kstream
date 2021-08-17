package com.frank.eventsourced.common.topics;

import lombok.ToString;
import org.apache.kafka.common.serialization.Serde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.singletonMap;

/**
 * Keeps information about serializer and deserializer of a topic
 *
 * @author ftorriani
 */
@ToString
public class TopicSerDe<K, V> {

    private final String name;
    private final Serde<K> keySerde;
    private final Serde<V> valueSerde;

    public TopicSerDe(String name, Serde<K> keySerde, Serde<V> valueSerde) {
        this.name = name;
        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
    }

    public Serde<K> keySerde() {
        return keySerde;
    }

    public Serde<V> valueSerde() {
        return valueSerde;
    }

    public String name() {
        return name;
    }

    public void configureKeySerDe(String schemaRegistryUrl) {
        keySerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false);
    }

    public void configureValueSerDe(String schemaRegistryUrl) {
        valueSerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false);
    }
}
