package com.frank.eventsourced.common.utils;

import com.frank.eventsourced.common.Service;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
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

@Log4j2
public class ClientUtils {

    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String DEFAULT_SCHEMA_REGISTRY_URL = "http://localhost:8081";
    public static final long MIN = 60 * 1000L;

    public static class CustomRocksDBConfig implements RocksDBConfigSetter {

        @Override
        public void setConfig(final String storeName, final Options options,
                              final Map<String, Object> configs) {
            // Workaround: We must ensure that the parallelism is set to >= 2.  There seems to be a known
            // issue with RocksDB where explicitly setting the parallelism to 1 causes issues (even though
            // 1 seems to be RocksDB's default for this configuration).
            int compactionParallelism = Math.max(Runtime.getRuntime().availableProcessors(), 2);
            // Set number of compaction threads (but not flush threads).
            options.setIncreaseParallelism(compactionParallelism);
        }
    }

    public static Properties streamsConfig(String bootstrapServers, String stateDir, String appId,
                                           String schemaRegistryUrl) {
        Properties props = new Properties();
        // Workaround for a known issue with RocksDB in environments where you have only 1 cpu core.
        props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // Limits the deduplication cache to 50Kb
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "50000");

        // commits often
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);

        // Maximize options to recover from a standby replica of the changelog
        props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 2);

        // Deserialize an AVRO class
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
//        props.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
//        props.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName() );

        // https://github.com/confluentinc/schema-registry/pull/680
//        props.put( StreamsConfig.producerPrefix( KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY ),
//                TopicRecordNameStrategy.class.getName() );

        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return props;
    }

    /**
     * Creates a Kafka producer optimized for durability
     *
     * @param bootstrapServers the broker csv
     * @param schemaRegistryUrl the schema registryURL
     * @param clientId the provided clientID
     * @param transactionId the provided transation ID
     * @param <T> the object to be published
     * @return a configured (for durability) Kafka producer
     */
    public static <T> Producer<String, T> startDurabilityOptimizedProducer(String bootstrapServers,
                                                                           String schemaRegistryUrl,
                                                                           String clientId,
                                                                           String transactionId) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

        // all broker should ack
        props.put(ProducerConfig.ACKS_CONFIG, "all");


        // Transaction support
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionId);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        props.put(ProducerConfig.RETRIES_CONFIG, MAX_VALUE);

        // con idempotent producer potremmo anche usare il valore di default
        // props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        // Serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());



        // https://github.com/confluentinc/schema-registry/pull/680
        props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
                TopicRecordNameStrategy.class.getName());

        // FIXME capire perchè non funziona l'assegnazione specifica di un serializer...
        Producer<String, T> prod = new KafkaProducer<String, T>(props);
//        KafkaProducer prod = new KafkaProducer<>( props,
//                topic.specificKeySerializer() != null ? topic.specificKeySerializer(): topic.keySerde().serializer(),
//                topic.specificValueSerializer() != null ? topic.specificValueSerializer() : topic.valueSerde().serializer() );
        prod.initTransactions();
        return prod;
    }

    public static void addShutdownHook(Service service) throws InterruptedException {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> service.stop());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                service.stop();
            } catch (Exception ignored) {
            }
        }));
    }
}