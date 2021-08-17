package com.frank.eventsourced.common.eventsourcing;

import com.frank.eventsourced.common.commands.beans.Command;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class CommandTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        Command command = (Command) record.value();

        return partitionTime;
    }
}
