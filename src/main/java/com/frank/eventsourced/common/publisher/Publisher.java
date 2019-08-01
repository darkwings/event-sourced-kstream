package com.frank.eventsourced.common.publisher;

import com.frank.eventsourced.common.utils.ClientUtils;
import com.frank.eventsourced.common.utils.EventUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collection;

import static com.frank.eventsourced.common.utils.ClientUtils.startProducer;

/**
 * @author ftorriani
 */
@Slf4j
public class Publisher {

    private KafkaProducer<String, SpecificRecord> producer;

    private String bootstrapServers;
    private String schemaRegistryUrl;
    private String clientId;
    private String transactionId;

    public Publisher( String bootstrapServers,
                      String schemaRegistryUrl,
                      String clientId,
                      String transactionId ) {
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.clientId = clientId;
        this.transactionId = transactionId;
        this.producer = startProducer( bootstrapServers, schemaRegistryUrl, clientId, transactionId );
    }

    public void publish( String topic, Collection<SpecificRecord> specificRecords ) {
        producer.beginTransaction();
        try {

            for ( SpecificRecord record : specificRecords ) {
                EventUtils.keyOf( record ).ifPresent( key -> {
                    producer.send( new ProducerRecord<>( topic, null, key, record ) );
                } );
            }
            producer.commitTransaction();
        }
        catch ( Exception e ) {
            log.error( "Error in transaction", e );
            producer.abortTransaction();
        }
    }


}
