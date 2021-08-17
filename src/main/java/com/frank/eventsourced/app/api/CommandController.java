package com.frank.eventsourced.app.api;

import com.frank.eventsourced.app.commands.beans.AddWidgetCommand;
import com.frank.eventsourced.app.commands.beans.CreateAppCommand;
import com.frank.eventsourced.common.commands.dispatcher.CommandDispatcher;
import com.frank.eventsourced.common.commands.dispatcher.Result;
import com.frank.eventsourced.common.exceptions.CommandException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.fromCompletionStage;


/**
 * @author ftorriani
 */
@RestController
@Log4j2
public class CommandController {

    @Autowired
    private CommandDispatcher commandDispatcher;

    @PostMapping(value = "/app/{tenantId}/{userId}",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Mono<Result> create(@PathVariable("tenantId") String tenantId,
                               @PathVariable("userId") String userId) throws CommandException {
        CreateAppCommand command = CreateAppCommand.builder().
                tenantId(tenantId).
                userId(userId).
                build();

        return fromCompletionStage(commandDispatcher.dispatch(command));
    }

    @PostMapping(value = "/app/{tenantId}/{userId}/widgets",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Mono<Result> createItem(@PathVariable("tenantId") String tenantId,
                                   @PathVariable("userId") String userId,
                                   @RequestBody WidgetBean itemBean) throws CommandException {
        AddWidgetCommand command = AddWidgetCommand.builder().
                tenantId(tenantId).userId(userId).
                widgetId(itemBean.getWidgetId()).
                version(itemBean.getVersion()).
                meta(itemBean.getMeta()).
                data(itemBean.getData()).build();

        return fromCompletionStage(commandDispatcher.dispatch(command));
    }
}
