package com.frank.eventsourced.common.publisher;

import com.frank.eventsourced.common.utils.EventUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static com.frank.eventsourced.common.utils.ClientUtils.startDurabilityOptimizedProducer;

/**
 * @author ftorriani
 */
@Log4j2
@Component
public class Publisher {

    private final Producer<String, SpecificRecord> producer;

    public Publisher(@Value("${bootstrap.servers}") String bootstrapServers,
                     @Value("${schema.registry.url}") String schemaRegistryUrl,
                     @Value("${client.id}") String clientId,
                     @Value("${transaction.id}") String transactionId) {
        this.producer = startDurabilityOptimizedProducer(bootstrapServers, schemaRegistryUrl, clientId, transactionId);
    }

    public void publish(String topic, Collection<SpecificRecord> specificRecords) {
        producer.beginTransaction();
        try {

            for (SpecificRecord record : specificRecords) {
                EventUtils.keyOf(record).ifPresent(key -> {
                    producer.send(new ProducerRecord<>(topic, null, key, record));
                });
            }
            producer.commitTransaction();
        } catch (Exception e) {
            log.error("Error in transaction", e);
            producer.abortTransaction();
        }
    }
}
