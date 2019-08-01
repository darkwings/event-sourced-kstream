package com.frank.eventsourced.app.commands.dispatcher;

import com.frank.eventsourced.common.commands.dispatcher.GenericCommandDispatcher;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.model.app.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ftorriani
 * <p>
 */
@Component
@Slf4j
public class AppCommandDispatcher extends GenericCommandDispatcher<App> {

    @Autowired
    public AppCommandDispatcher( @Qualifier("appService") EventSourcingService<App> service,
                                 RestTemplate restTemplate ) {
        super( service, restTemplate );
    }
}
