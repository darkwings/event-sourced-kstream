package com.frank.eventsourced.common.topics;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import lombok.ToString;
import org.apache.kafka.common.serialization.Serde;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;
import static java.util.Collections.singletonMap;

/**
 * Keeps information about serializer and deserializer of a topic and, optionally, how the SerDes will interact
 * with schema registry
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

    /**
     * @return the key SerDe
     */
    public Serde<K> keySerde() {
        return keySerde;
    }

    /**
     * @return the value SerDe
     */
    public Serde<V> valueSerde() {
        return valueSerde;
    }

    /**
     * @return the topic name
     */
    public String name() {
        return name;
    }

    /**
     * Configures the SerDes of the key of this topic as bound to use of schema registry with default schema naming
     *
     * @param schemaRegistryUrl the value for {@link AbstractKafkaSchemaSerDeConfig#SCHEMA_REGISTRY_URL_CONFIG}. Could
     *                          not be {@code null}
     *
     * @throws NullPointerException when one parameter is {@code null}
     */
    public void configureKeySerDe(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl);
        keySerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false);
    }

    /**
     * Configures the SerDes of the value of this topic as bound to use of schema registry with default schema naming
     *
     * @param schemaRegistryUrl the value for {@link AbstractKafkaSchemaSerDeConfig#SCHEMA_REGISTRY_URL_CONFIG}. Could
     *                          not be {@code null}
     *
     * @throws NullPointerException when one parameter is {@code null}
     */
    public void configureValueSerDe(String schemaRegistryUrl) {
        Objects.requireNonNull(schemaRegistryUrl);
        valueSerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl), false);
    }

    /**
     * Configures how the SerDes of this topic will interact (when managing the value) with schema registry
     * in relation to schema naming.
     * The value of the parameter {@code valueSchemaNamingPolicy} should be one of the fully qualified names
     * allowed
     * <ul>
     *     <li>{@link TopicRecordNameStrategy}</li>
     *     <li>{@link RecordNameStrategy}</li>
     *     <li>{@link TopicNameStrategy}</li>
     * </ul>
     *
     * @param schemaRegistryUrl the value for {@link AbstractKafkaSchemaSerDeConfig#SCHEMA_REGISTRY_URL_CONFIG}. Could
     *                          not be {@code null}
     * @param valueSchemaNamingPolicy the value for {@link AbstractKafkaSchemaSerDeConfig#VALUE_SUBJECT_NAME_STRATEGY}. Could
     *                                not be {@code null}
     * @throws NullPointerException when one parameter is {@code null}
     * @throws IllegalArgumentException when the valueSchemaNamingPolicy parameters
     */
    public void configureValueSerDeAndSchemaNaming(String schemaRegistryUrl, String valueSchemaNamingPolicy) {
        Objects.requireNonNull(schemaRegistryUrl);
        Objects.requireNonNull(valueSchemaNamingPolicy);
        if (!valueSchemaNamingPolicy.equals(TopicRecordNameStrategy.class.getName()) &&
                !valueSchemaNamingPolicy.equals(RecordNameStrategy.class.getName()) &&
                !valueSchemaNamingPolicy.equals(TopicNameStrategy.class.getName())) {
            throw new IllegalArgumentException("value schema naming policy should be one of TopicRecordNameStrategy|RecordNameStrategy|TopicNameStrategy (fully qualified name)");
        }

        Map<String, Object> conf = new HashMap<>();
        conf.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        conf.put(VALUE_SUBJECT_NAME_STRATEGY, valueSchemaNamingPolicy);
        valueSerde.configure(conf, false);
    }
}
