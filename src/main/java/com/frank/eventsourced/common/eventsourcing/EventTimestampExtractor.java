package com.frank.eventsourced.common.eventsourcing;

import com.frank.eventsourced.common.utils.EventUtils;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class EventTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        SpecificRecord event = (SpecificRecord) record.value();
        if (event != null) {
            return EventUtils.timestampOf(event, partitionTime);
        }
        return partitionTime;
    }
}
