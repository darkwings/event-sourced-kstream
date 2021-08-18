package com.frank.eventsourced.app.service;

import com.frank.eventsourced.common.commands.handler.CommandHandler;
import com.frank.eventsourced.common.events.EventHandler;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;
import com.frank.eventsourced.common.publisher.Publisher;
import com.frank.eventsourced.common.topics.Topics;
import com.frank.eventsourced.common.utils.AvroJsonConverter;
import com.frank.eventsourced.app.utils.KeyBuilder;
import com.frank.eventsourced.model.app.App;

import org.apache.kafka.streams.kstream.Initializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ftorriani
 */
@Component
@Qualifier("appService")
public class AppService extends EventSourcingService<App> {

    private static final String APP_STATE_STORE_NAME = "app-store";

    @Autowired
    public AppService(@Qualifier("appCommand") CommandHandler<App> commandHandler,
                      @Qualifier("appEvent") EventHandler<App> eventHandler,
                      @Value("${bootstrap.servers}") String bootstrapServers,
                      @Value("${schema.registry.url}") String schemaRegistryUrl,
                      @Value("${state.dir}") String stateDir,
                      @Value("${server.host:localhost}") String serverHost,
                      @Value("${server.port}") int serverPort,
                      @Qualifier("appTopics") Topics<App> topics,
                      RestTemplate restTemplate,
                      Publisher publisher,
                      @Value("${app.stream.name:app-stream}") String streamName) {
        super(bootstrapServers,
                schemaRegistryUrl,
                stateDir,
                serverHost,
                serverPort,
                restTemplate,
                topics,
                eventHandler,
                commandHandler,
                publisher,
                streamName);
    }

    @Override
    protected AvroJsonConverter<App> avroJsonConverter() {
        return AvroJsonConverter.fromClass(App.class);
    }

    @Override
    protected String stateStoreName() {
        return APP_STATE_STORE_NAME;
    }

    @Override
    protected String remoteStateUrl(String key, HostStoreInfo hostStoreInfo) {
        String[] splitKey = KeyBuilder.split(key);
        return "http://" + hostStoreInfo.getHost() + ":" + hostStoreInfo.getPort() +
                "/app/" + splitKey[0] + "/" + splitKey[1];
    }

    @Override
    @Deprecated
    protected Initializer<App> initializer() {
        return App::new;
    }
}
