package com.frank.eventsourced.common.commands.dispatcher;

import com.frank.eventsourced.common.commands.beans.Command;
import com.frank.eventsourced.common.exceptions.CommandException;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;

import java.util.concurrent.CompletableFuture;

/**
 * @author ftorriani
 */
public interface CommandDispatcher {

    default String remoteCommandConnectorUrl(HostStoreInfo hostStoreInfo) {
        return "http://" + hostStoreInfo.getHost() + ":" + hostStoreInfo.getPort() +
                "/command";
    }

    CompletableFuture<Result> dispatch(Command command) throws CommandException;
}
