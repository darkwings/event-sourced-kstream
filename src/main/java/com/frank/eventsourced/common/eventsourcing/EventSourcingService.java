package com.frank.eventsourced.common.eventsourcing;

import com.frank.eventsourced.common.Service;
import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.commands.handler.CommandHandler;
import com.frank.eventsourced.common.events.EventHandler;
import com.frank.eventsourced.common.exceptions.CommandException;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;
import com.frank.eventsourced.common.interactivequeries.StreamsMetadataService;
import com.frank.eventsourced.common.publisher.Publisher;
import com.frank.eventsourced.common.topics.Topics;
import com.frank.eventsourced.common.topics.TopicSerDe;
import com.frank.eventsourced.common.utils.AvroJsonConverter;
import com.frank.eventsourced.common.utils.ClientUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;
import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;
import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
import static org.apache.kafka.streams.state.StreamsMetadata.NOT_AVAILABLE;

/**
 * Base class for event sourcing service
 *
 * @param <A> the state (the aggregate)
 */
@Log4j2
public abstract class EventSourcingService<A extends SpecificRecord> implements Service {

    private static final String CALL_TIMEOUT = "10000";

    private final String serviceId;
    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String stateDir;
    private final String serverHost;
    private final int serverPort;

    private StreamsMetadataService streamsMetadataService;
    private final KafkaStreams streams;

    private final EventHandler<A> eventHandler;
    private final CommandHandler<A> commandHandler;

    private final RestTemplate restTemplate;

    private final AvroJsonConverter<A> avroJsonConverter;

    private final TopicSerDe<String, SpecificRecord> eventLog;
    private final TopicSerDe<String, A> stateTopic;

    private final Publisher publisher;

    protected EventSourcingService(String bootstrapServers,
                                   String schemaRegistryUrl,
                                   String stateDir,
                                   String serverHost,
                                   int serverPort,
                                   RestTemplate restTemplate,
                                   Topics<A> topics,
                                   EventHandler<A> eventHandler,
                                   CommandHandler<A> commandHandler,
                                   Publisher publisher, String streamName) {

        this.serviceId = getClass().getName();

        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.stateDir = stateDir;
        this.eventHandler = eventHandler;
        this.commandHandler = commandHandler;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.restTemplate = restTemplate;

        this.stateTopic = topics.stateTopic();
        this.eventLog = topics.eventLogTopic();

        this.publisher = publisher;

        avroJsonConverter = avroJsonConverter();

        streams = startKStreams();
        try {
            ClientUtils.addShutdownHook(this);
        } catch (InterruptedException e) {
            log.error("Error ", e);
        }
    }

    protected abstract AvroJsonConverter<A> avroJsonConverter();

    protected abstract String stateStoreName();

    private KafkaStreams startKStreams() {
        KafkaStreams streams = new KafkaStreams(createTopology().build(), streamsConfig());
        streamsMetadataService = new StreamsMetadataService(streams);
        streams.setUncaughtExceptionHandler(exception -> {
            log.error("Streams exception ", exception);
            return REPLACE_THREAD;
        });
        streams.setStateListener((newState, oldState) -> {
            log.info("Setting new state {} (old was {})", newState, oldState);
            if (newState == KafkaStreams.State.REBALANCING) {
                // Do anything that's necessary to manage rebalance
                log.info("Rebalance in progress");
            }
        });
        streams.start();
        return streams;
    }

    private StreamsBuilder createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // State table
        KTable<String, A> stateTable =
                builder.table(stateTopic.name(), Consumed.with(stateTopic.keySerde(), stateTopic.valueSerde()),
                        Materialized.as(stateStoreName()));

        // Event stream is in left join with the State Table
        builder.stream(eventLog.name(),
                        Consumed.with(eventLog.keySerde(), eventLog.valueSerde())
                                .withOffsetResetPolicy(LATEST)
                                .withTimestampExtractor(new EventTimestampExtractor())).
                leftJoin(stateTable, eventHandler::apply).
                // State topic
                to(stateTopic.name(), Produced.with(stateTopic.keySerde(), stateTopic.valueSerde()));

        return builder;
    }

    protected abstract Initializer<A> initializer();

//    private StreamsBuilder createTopology() {
//        StreamsBuilder builder = new StreamsBuilder();
//
//        builder.stream(eventLog.name(),
//                        Consumed.with(eventLog.keySerde(), eventLog.valueSerde())
//                                .withOffsetResetPolicy(LATEST)
//                                .withTimestampExtractor(new EventTimestampExtractor())).
//                groupByKey().
//                aggregate(initializer(), (key, event, state) -> eventHandler.apply(event, state),
//                        Materialized.<String, S, KeyValueStore<Bytes, byte[]>>as(stateStoreName()).
//                                withKeySerde(stateTopic.keySerde()).
//                                withValueSerde(stateTopic.valueSerde()));
//        return builder;
//    }

    private Properties streamsConfig() {
        Properties props = ClientUtils.streamsConfig(bootstrapServers, stateDir,
                serviceId, schemaRegistryUrl);
        log.info("Setting serverHost: {} and port {}", serverHost, serverPort);
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, serverHost + ":" + serverPort);
        return props;
    }

    private ReadOnlyKeyValueStore<String, A> viewStore() {
        return streams.store(fromNameAndType(stateStoreName(), QueryableStoreTypes.keyValueStore()));
    }

    private Optional<HostStoreInfo> getKeyLocation(String key) {
        Optional<HostStoreInfo> opt = hostForKey(key);
        int retry = 0;
        if (opt.isPresent()) {
            HostStoreInfo locationOfKey = opt.get();
            while (NOT_AVAILABLE.host().equals(locationOfKey.getHost())
                    && NOT_AVAILABLE.port() == locationOfKey.getPort()) {
                //The metastore is not available. This can happen on startup/rebalance.
                if (retry >= 10) {
                    log.warn("Reached the maximum number of retry. {} is starting or it is rebalancing",
                            serviceId);
                    return Optional.empty();
                }
                retry++;
                try {
                    // Sleep a bit until metadata becomes available
                    Thread.sleep(Math.min(Long.valueOf(CALL_TIMEOUT), 200));
                } catch (InterruptedException e) {
                }
            }
            return opt;
        } else {
            return Optional.empty();
        }
    }

    public boolean isLocal(HostStoreInfo host) {
        return host.getHost().equals(serverHost) && host.getPort() == serverPort;
    }

    /**
     * Return the location of the aggregate with the given key
     *
     * @param key the state key
     * @return the host store info
     */
    public Optional<HostStoreInfo> hostForKey(String key) {
        return streamsMetadataService
                .streamsMetadataForStoreAndKey(stateStoreName(), key,
                        Serdes.String().serializer());
    }


    /**
     * Handles a command
     *
     * @param command the command
     * @throws CommandException if there is any problem submitting the command
     */
    public void handleCommand(Command command) throws CommandException {
        commandHandler.apply(command, viewStore().get(command.aggregateId())).
                ifPresent(event -> {
                    log.info("handleCommand() for aggregate '{}'. Processed command {}. " +
                                    "Publishing event '{}' on topic '{}'",
                            command.aggregateId(),
                            command.getClass().getSimpleName(),
                            event.getClass().getSimpleName(), eventLog.name());
                    publisher.publish(eventLog.name(), Collections.singletonList(event));
                });
    }

    /**
     * Retrieves the state. It uses interactive queries to determine which is the host
     * of the given key.
     *
     * @param aggregateId    the key to retrieve the state
     * @param localOperator  the operator to retrieve the local state
     * @param remoteOperator the operator to retrieve the remote state, given the URL to which the state is exposed
     * @param <T>            the object that represent the state
     * @return the state, as an {@link Optional}
     */
    private <T> Optional<T> internalStateRetrieve(String aggregateId,
                                                  Supplier<T> localOperator,
                                                  Function<String, T> remoteOperator) {
        Optional<HostStoreInfo> hostForKey = getKeyLocation(aggregateId);
        return hostForKey.map(hostStoreInfo -> {
            if (isLocal(hostStoreInfo)) {
                log.debug("State of aggregate {} is available locally", aggregateId);
                return localOperator.get();
            } else {
                String url = remoteStateUrl(aggregateId, hostStoreInfo);
                return remoteOperator.apply(url);
            }
        });
    }

    /**
     * Retrieves the current state identified by the given key
     *
     * @param aggregateId the state ID
     * @return the found State, if any
     */
    public Optional<A> state(String aggregateId) {
        return internalStateRetrieve(aggregateId,
                () -> viewStore().get(aggregateId),
                remoteUrl -> {
                    log.info("Aggregate '{}' is available on another instance. Forwarding query to '{}'",
                            aggregateId, remoteUrl);
                    try {
                        ResponseEntity<String> response = restTemplate.getForEntity(remoteUrl, String.class);
                        log.info("Aggregate '{}' Received response from {}", aggregateId, remoteUrl);
                        String json = response.getBody();
                        return avroJsonConverter.decodeJson(json);
                    } catch (HttpClientErrorException e) {
                        if (e.getRawStatusCode() == 404) {
                            log.info("Remote error: aggregate '{}' not existing on {}", aggregateId, remoteUrl);
                            return null;
                        } else {
                            throw new RemoteStateException("Client error while retrieving " +
                                    "remote aggregate '" + aggregateId + "'", e);
                        }
                    } catch (Exception e) {
                        throw new RemoteStateException("Failed to retrieve remote " +
                                "aggregate '" + aggregateId + "'", e);
                    }
                });
    }

    /**
     * Retrieves the current state, as JSON, identified by the given key
     *
     * @param aggregateId the state ID
     * @return the found State as JSON, if any
     */
    public Optional<String> stateAsJson(String aggregateId) {
        return internalStateRetrieve(aggregateId,
                () -> {
                    log.info("Aggregate '{}' is available locally", aggregateId);
                    try {
                        A record = viewStore().get(aggregateId);
                        return record != null ? avroJsonConverter.encodeToJson(record) : null;
                    } catch (IOException e) {
                        log.error("Failed to convert state", e);
                        return null;
                    }
                },
                remoteUrl -> {
                    log.info("Aggregate '{}' is available on another instance. Forwarding query to '{}'", aggregateId, remoteUrl);
                    try {
                        ResponseEntity<String> response = restTemplate.getForEntity(remoteUrl, String.class);
                        log.info("Aggregate '{}' Received response from {}", aggregateId, remoteUrl);
                        return response.getBody();
                    } catch (HttpClientErrorException e) {
                        if (e.getRawStatusCode() == 404) {
                            log.info("Remote error: aggregate '{}' not existing on {}", aggregateId, remoteUrl);
                            return null;
                        } else {
                            throw new RemoteStateException("Client error while retrieving " +
                                    "remote aggregate '" + aggregateId + "'", e);
                        }
                    } catch (Exception e) {
                        throw new RemoteStateException("Failed to retrieve remote " +
                                "aggregate '" + aggregateId + "'", e);
                    }
                });
    }


    protected abstract String remoteStateUrl(String key, HostStoreInfo hostStoreInfo);

    public List<HostStoreInfo> allInfo() {
        return streamsMetadataService.streamsMetadataForStore(stateStoreName());
    }


    @Override
    public void stop() {
        if (streams != null) {
            streams.close();
        }
    }
}
