package com.frank.eventsourced.app.commands.dispatcher;

import com.frank.eventsourced.common.commands.dispatcher.GenericCommandDispatcher;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.model.app.App;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author ftorriani
 * <p>
 */
@Component
@Log4j2
public class AppCommandDispatcher extends GenericCommandDispatcher<App> {

    @Autowired
    public AppCommandDispatcher(@Qualifier("appService") EventSourcingService<App> service,
                                RestTemplate restTemplate) {
        super(service, restTemplate);
    }
}
