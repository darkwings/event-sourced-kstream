package com.frank.eventsourced.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.commands.dispatcher.CommandDispatcher;
import com.frank.eventsourced.common.commands.dispatcher.CommandWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.lang.Class.forName;
import static reactor.core.publisher.Mono.fromCompletionStage;

/**
 * @author ftorriani
 */
@RestController
public class InternalCommandConnector {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CommandDispatcher commandDispatcher;

    @PostMapping("/command")
    @SuppressWarnings("unchecked")
    public Mono<String> receive(@RequestBody CommandWrapper wrapper) throws Exception {

        Class<? extends Command> klass = (Class<? extends Command>) forName(wrapper.getType());
        Command command = mapper.readValue(wrapper.getJson(), klass);

        // TODO a better error handling is needed

        // From here on (if the caller dit it well) it is a local call
        return fromCompletionStage(commandDispatcher.dispatch(command)).
                map(result -> result.getStatus());
    }
}
