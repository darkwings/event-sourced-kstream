package com.frank.eventsourced.common.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author ftorriani
 */
@Configuration
public class RemoteConfiguration {

    @Value("${custom.rest.connection.connection-request-timeout:5000}")
    private Integer connectionRequestTimeout;

    @Value("${custom.rest.connection.connect-timeout:5000}")
    private Integer connectTimeout;

    @Value("${custom.rest.connection.read-timeout:5000}")
    private Integer readTimeout;

    @Value("${custom.rest.connection.max-per-route:5}")
    private Integer defaultMaxPerRoute;

    @Value("${custom.rest.connection.max-total:20}")
    private Integer maxTotal;

    public HttpComponentsClientHttpRequestFactory customHttpRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute( defaultMaxPerRoute );
        connectionManager.setMaxTotal( maxTotal );

        HttpComponentsClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().
                        setConnectionManager( connectionManager ).
                        build() );

        rf.setConnectionRequestTimeout( connectionRequestTimeout );
        rf.setReadTimeout( readTimeout );
        rf.setConnectTimeout( connectTimeout );
        return rf;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate( customHttpRequestFactory() );
    }
}
