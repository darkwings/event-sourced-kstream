package com.frank.eventsourced.common.topics;

import com.frank.eventsourced.commands.platform.app.CommandFailure;
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

    TopicSerDe<String, CommandFailure> commandFailureTopic();
}
