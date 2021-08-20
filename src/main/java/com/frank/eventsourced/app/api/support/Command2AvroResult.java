package com.frank.eventsourced.app.api.support;

import lombok.Value;
import org.apache.avro.specific.SpecificRecord;

@Value
public class Command2AvroResult {

    SpecificRecord record;
    String operationId;
}
