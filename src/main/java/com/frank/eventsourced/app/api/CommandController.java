package com.frank.eventsourced.app.api;

import com.frank.eventsourced.app.api.bean.CommandResult;
import com.frank.eventsourced.app.api.bean.WidgetBean;
import com.frank.eventsourced.app.api.support.Command2Avro;
import com.frank.eventsourced.app.api.support.Command2AvroResult;
import com.frank.eventsourced.app.service.AppService;
import com.frank.eventsourced.common.exceptions.CommandException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

import static com.frank.eventsourced.app.api.bean.CommandResult.Outcome.KO;
import static com.frank.eventsourced.app.api.bean.CommandResult.Outcome.OK;


/**
 * @author ftorriani
 */
@RestController
@Log4j2
public class CommandController {

    @Autowired
    private AppService appService;

    @Autowired
    private Command2Avro command2Avro;

    @PostMapping(value = "/app/{tenantId}/{userId}",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Mono<CommandResult> create(@PathVariable("tenantId") String tenantId,
                                      @PathVariable("userId") String userId) throws CommandException {

        return publishCommand(() -> command2Avro.createCommand(tenantId, userId));
    }

    @DeleteMapping(value = "/app/{tenantId}/{userId}/{version}",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Mono<CommandResult> delete(@PathVariable("tenantId") String tenantId,
                                      @PathVariable("userId") String userId,
                                      @PathVariable("version") int version) throws CommandException {
        return publishCommand(() -> command2Avro.cancelCommand(tenantId, userId, version));
    }


    @PostMapping(value = "/app/{tenantId}/{userId}/widgets",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Mono<CommandResult> createItem(@PathVariable("tenantId") String tenantId,
                                          @PathVariable("userId") String userId,
                                          @RequestBody WidgetBean widgetBean) throws CommandException {
        return publishCommand(() -> command2Avro.addWidgetCommand(tenantId, userId, widgetBean));
    }

    private Mono<CommandResult> publishCommand(Supplier<Command2AvroResult> supplier) {
        return Mono.just(supplier.get())
                .map(c -> {
                    appService.publishCommand(c.getRecord());
                    return c;
                })
                .map(command2AvroResult -> new CommandResult(OK, command2AvroResult.getOperationId()))
                .doOnError(t -> {
                    log.error("Failed to publish command", t);
                })
                .onErrorReturn(new CommandResult(KO, "NONE"));
    }
}
