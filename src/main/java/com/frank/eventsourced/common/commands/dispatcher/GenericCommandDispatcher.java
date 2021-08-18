package com.frank.eventsourced.common.commands.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.common.exceptions.CommandError;
import com.frank.eventsourced.common.exceptions.CommandException;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import static com.frank.eventsourced.app.utils.KeyBuilder.keyOf;
import static com.frank.eventsourced.common.exceptions.CommandError.GENERIC_ERROR;
import static com.frank.eventsourced.common.exceptions.CommandError.NOT_EXISTING_AGGREGATE;

/**
 * @author ftorriani
 *
 * @param <A> the state (the aggregate)
 */
@Log4j2
public abstract class GenericCommandDispatcher<A extends SpecificRecord> implements CommandDispatcher {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final EventSourcingService<A> service;
    private final RestTemplate restTemplate;

    public GenericCommandDispatcher(EventSourcingService<A> service,
                                    RestTemplate restTemplate) {
        this.service = service;
        this.restTemplate = restTemplate;
    }

    public CompletableFuture<Result> dispatch(Command command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String key = keyOf(command).
                        orElseThrow(() -> new RuntimeException("Missing key in command " + command));
                HostStoreInfo hostStoreInfo = service.hostForKey(key).
                        orElseThrow(() -> new CommandException("Invalid key", NOT_EXISTING_AGGREGATE));
                if (service.isLocal(hostStoreInfo)) {

                    log.info("Aggregate '{}' is located here. Applying command {}", key, command.getClass().getName());

                    service.handleCommand(command);
                    return new Result("OK");
                } else {
                    String url = remoteCommandConnectorUrl(hostStoreInfo);

                    log.info("Aggregate '{}' is not located here. Forwarding command '{}' to " +
                                    "remote command controller '{}'", key,
                            command.getClass().getName(), url);

                    try {
                        CommandWrapper wrapper = new CommandWrapper(command.getClass().getName(),
                                mapper.writeValueAsString(command));
                        ResponseEntity<String> response = restTemplate.
                                postForEntity(url, wrapper, String.class);
                        log.info("Aggregate '{}': received response from {}. API call returned HTTP {}",
                                command.aggregateId(), url, response.getStatusCodeValue());
                        return new Result("OK");
                    } catch (HttpClientErrorException e) {
                        log.error("Aggregate '{}' Remote API error: instance {} returned HTTP {}", command.aggregateId(),
                                url, e.getRawStatusCode());
                        switch (e.getRawStatusCode()) {
                            case 409:
                                Result result = mapper.readValue(e.getResponseBodyAsString(), Result.class);
                                throw new CommandException("Detected remote conflict for aggregate " + key,
                                        CommandError.valueOf(result.getStatus()));
                            case 404:
                                throw new CommandException("Detected remote missing aggregate " + key,
                                        NOT_EXISTING_AGGREGATE);

                            default:
                                throw new CommandException("Detected remote error for aggregate " + key,
                                        GENERIC_ERROR);
                        }
                    }
                }
            } catch (CommandException e) {
                throw e;
            } catch (Exception e) {
                throw new CommandException("Error handling command " + command.getClass().getName() +
                        " for aggregate " + command.aggregateId() + ". Error is: " + e.getMessage(), e);
            }
        });
    }
}
