package com.frank.eventsourced.common.topics;

import org.apache.avro.specific.SpecificRecord;

/**
 * Topics configuration
 *
 * @param <A> the aggregate class
 */
public interface Topics<A extends SpecificRecord> {

    TopicSerDe<String, SpecificRecord> commandTopic();

    TopicSerDe<String, SpecificRecord> eventLogTopic();

    TopicSerDe<String, A> stateTopic();
}
