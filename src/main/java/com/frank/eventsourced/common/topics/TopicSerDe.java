package com.frank.eventsourced.common.topics;

import com.frank.eventsourced.app.schema.Schema;
import org.apache.kafka.common.serialization.Serde;

/**
 * @author ftorriani
 */
public class TopicSerDe<K, V> {

    private String name;
    private Serde<K> keySerde;
    private Serde<V> valueSerde;

    public TopicSerDe(String name, Serde<K> keySerde, Serde<V> valueSerde) {
        this.name = name;
        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
        Schema.Topics.ALL.put(name, this);
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

    public String toString() {
        return name;
    }
}
