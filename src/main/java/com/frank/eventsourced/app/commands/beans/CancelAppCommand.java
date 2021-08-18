package com.frank.eventsourced.app.commands.beans;

import lombok.Builder;

public class CancelAppCommand extends AbstractCommand {

    @Builder
    public CancelAppCommand(String tenantId, String userId, Integer version) {
        super(tenantId, userId, version);
    }
}
