package com.frank.eventsourced.common.interactivequeries;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.StreamsMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Looks up StreamsMetadata from KafkaStreams and converts the results
 * into Beans that can be JSON serialized via Jersey.
 */
public class StreamsMetadataService {

    private final KafkaStreams streams;

    public StreamsMetadataService(final KafkaStreams streams) {
        this.streams = streams;
    }

    /**
     * Get the metadata for all instances of this Kafka Streams application that currently
     * has the provided store.
     *
     * @param store The store to locate
     * @return List of {@link HostStoreInfo}
     */
    public List<HostStoreInfo> streamsMetadataForStore(final String store) {
        // Get metadata for all of the instances of this Kafka Streams application hosting the store
        final Collection<StreamsMetadata> metadata = streams.allMetadataForStore(store);
        return mapInstancesToHostStoreInfo(metadata);
    }

    /**
     * Find the metadata for the instance of this Kafka Streams Application that has the given
     * store and would have the given key if it exists.
     *
     * @param store Store to find
     * @param key   The key to find
     * @return {@link HostStoreInfo}
     */
    public <K> Optional<HostStoreInfo> streamsMetadataForStoreAndKey(final String store,
                                                                     final K key,
                                                                     final Serializer<K> serializer) {
        // Get metadata for the instances of this Kafka Streams application hosting the store and
        // potentially the value for key
        final KeyQueryMetadata metadata = streams.queryMetadataForKey(store, key, serializer);
        if (metadata == null) {
            return Optional.empty();
        }
        HostInfo hostInfo = metadata.activeHost();

        return Optional.of(new HostStoreInfo(hostInfo.host(), hostInfo.port()));
    }

    private List<HostStoreInfo> mapInstancesToHostStoreInfo(
            final Collection<StreamsMetadata> metadata) {
        return metadata.stream().map(md -> new HostStoreInfo(md.host(), md.port()))
                .collect(Collectors.toList());
    }
}
