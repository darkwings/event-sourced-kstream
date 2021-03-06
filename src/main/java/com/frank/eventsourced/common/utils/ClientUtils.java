package com.frank.eventsourced.common.utils;

import com.frank.eventsourced.common.Service;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static java.lang.Integer.MAX_VALUE;

public class ClientUtils {

    private static final Logger log = LoggerFactory.getLogger( ClientUtils.class );
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://localhost:8081";
    public static final long MIN = 60 * 1000L;


    public static class CustomRocksDBConfig implements RocksDBConfigSetter {

        @Override
        public void setConfig( final String storeName, final Options options,
                               final Map<String, Object> configs ) {
            // Workaround: We must ensure that the parallelism is set to >= 2.  There seems to be a known
            // issue with RocksDB where explicitly setting the parallelism to 1 causes issues (even though
            // 1 seems to be RocksDB's default for this configuration).
            int compactionParallelism = Math.max( Runtime.getRuntime().availableProcessors(), 2 );
            // Set number of compaction threads (but not flush threads).
            options.setIncreaseParallelism( compactionParallelism );
        }
    }

    public static Properties streamsConfig( String bootstrapServers, String stateDir,
                                            String appId,
                                            String schemaRegistryUrl ) {
        Properties props = new Properties();
        // Workaround for a known issue with RocksDB in environments where you have only 1 cpu core.
        props.put( StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class );
        props.put( StreamsConfig.APPLICATION_ID_CONFIG, appId );
        props.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        props.put( StreamsConfig.STATE_DIR_CONFIG, stateDir );
        props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" );
        props.put( StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once" );
        props.put( StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1 ); //commit as fast as possible

        // Let's keep a number of standby replicas for each storage, rebalancing should be fast
        props.put( StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 0 );

        // Deserialize an AVRO class
        props.put( KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true" );
//        props.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
//        props.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName() );

        // https://github.com/confluentinc/schema-registry/pull/680
//        props.put( StreamsConfig.producerPrefix( KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY ),
//                TopicRecordNameStrategy.class.getName() );

        props.put( KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl );
        return props;
    }

    public static <T> KafkaProducer<String, T> startProducer( String bootstrapServers,
                                                              String clientId,
                                                              String schemaRegistryUrl,
                                                              String transactionId ) {
        Properties props = new Properties();
        props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        props.put( ProducerConfig.ACKS_CONFIG, "all" );
        props.put( ProducerConfig.CLIENT_ID_CONFIG, clientId );
        props.put( ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionId );
        props.put( ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true" );
        props.put( ProducerConfig.RETRIES_CONFIG, MAX_VALUE );
        props.put( ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1 );

        // Serializers
        props.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        props.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName() );

        props.put( KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl );

        // https://github.com/confluentinc/schema-registry/pull/680
        props.put( KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
                TopicRecordNameStrategy.class.getName() );

        // FIXME capire perchè non funziona l'assegnazione specifica di un serializer...
        KafkaProducer prod = new KafkaProducer<String, T>( props );
//        KafkaProducer prod = new KafkaProducer<>( props,
//                topic.specificKeySerializer() != null ? topic.specificKeySerializer(): topic.keySerde().serializer(),
//                topic.specificValueSerializer() != null ? topic.specificValueSerializer() : topic.valueSerde().serializer() );
        prod.initTransactions();
        return prod;
    }

    public static void addShutdownHook( Service service ) throws InterruptedException {
        Thread.currentThread().setUncaughtExceptionHandler( ( t, e ) -> service.stop() );
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            try {
                service.stop();
            }
            catch ( Exception ignored ) {
            }
        } ) );
    }
}