package com.frank.eventsourced.app.service;

import com.frank.eventsourced.common.commands.handler.CommandHandler;
import com.frank.eventsourced.common.events.EventHandler;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;
import com.frank.eventsourced.common.publisher.Publisher;
import com.frank.eventsourced.common.utils.AvroJsonConverter;
import com.frank.eventsourced.app.schema.Schema;
import com.frank.eventsourced.app.utils.KeyBuilder;
import com.frank.eventsourced.model.app.App;

import org.apache.kafka.streams.kstream.Initializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ftorriani
 */
@Component
@DependsOn("schemas")
@Qualifier("appService")
public class AppService extends EventSourcingService<App> {

    private static final String APP_STATE_STORE_NAME = "app-store";

    @Autowired
    @Qualifier("appCommand")
    private CommandHandler<App> commandHandler;

    @Autowired
    @Qualifier("appEvent")
    private EventHandler<App> eventHandler;

    @Autowired
    public AppService( @Value("${bootstrap.servers}") String bootstrapServers,
                       @Value("${schema.registry.url}") String schemaRegistryUrl,
                       @Value("${state.dir}") String stateDir,
                       @Value("${client.id}") String clientId,
                       @Value("${server.host:localhost}") String serverHost,
                       @Value("${server.port}") int serverPort,
                       RestTemplate restTemplate,
                       EventHandler<App> eventHandler,
                       CommandHandler<App> commandHandler, Publisher publisher,
                       @Value("${app.stream.name:app-stream}") String streamName ) {
        super( bootstrapServers,
                schemaRegistryUrl,
                stateDir,
                clientId,
                serverHost,
                serverPort,
                restTemplate,
                Schema.Topics.APP_EVENTS,
                Schema.Topics.APP_VIEW,
                eventHandler,
                commandHandler,
                publisher,
                streamName );
    }

    @Override
    protected AvroJsonConverter<App> avroJsonConverter() {
        return AvroJsonConverter.fromClass( App.class );
    }

    @Override
    protected String stateStoreName() {
        return APP_STATE_STORE_NAME;
    }

    @Override
    protected String remoteStateUrl( String key, HostStoreInfo hostStoreInfo ) {
        String[] unkey = KeyBuilder.split( key );
        return "http://" + hostStoreInfo.getHost() + ":" + hostStoreInfo.getPort() +
                "/app/" + unkey[ 0 ] + "/" + unkey[ 1 ];
    }

    @Override
    protected Initializer<App> initializer() {
        return App::new;
    }
}
