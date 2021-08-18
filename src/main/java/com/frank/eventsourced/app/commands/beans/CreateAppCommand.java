package com.frank.eventsourced.app.commands.beans;

import lombok.Builder;
import lombok.Getter;

/**
 * @author ftorriani
 */
@Getter
public class CreateAppCommand extends AbstractCommand {

    @Builder
    public CreateAppCommand(String tenantId, String userId) {
        super(tenantId, userId, 0);
    }
}