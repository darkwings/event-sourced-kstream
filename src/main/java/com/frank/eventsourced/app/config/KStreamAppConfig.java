package com.frank.eventsourced.app.config;


import com.frank.eventsourced.common.publisher.Publisher;
import com.frank.eventsourced.app.schema.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author ftorriani
 */
@Configuration
public class KStreamAppConfig {

    @Bean
    public Schema schemas( @Value("${schema.registry.url}") String schemaRegistryUrl ) {
        Schema schemas = new Schema( schemaRegistryUrl );
        schemas.configureSerdesWithSchemaRegistryUrl();
        return schemas;
    }

    @Bean
    public Publisher publisher( @Value("${bootstrap.servers}") String bootstrapServers,
                                @Value("${schema.registry.url}") String schemaRegistryUrl,
                                @Value("${client.id}") String clientId,
                                @Value("${transaction.id}") String transactionId ) throws IOException {

        return new Publisher( bootstrapServers, clientId, schemaRegistryUrl, transactionId );
    }


}
