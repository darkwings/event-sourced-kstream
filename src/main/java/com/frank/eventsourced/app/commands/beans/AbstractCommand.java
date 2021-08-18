package com.frank.eventsourced.app.commands.beans;

import com.frank.eventsourced.common.commands.beans.Command;
import lombok.Getter;
import lombok.ToString;

import java.time.Clock;

import static com.frank.eventsourced.app.utils.KeyBuilder.key;

@ToString
@Getter
public abstract class AbstractCommand implements Command {

    protected String tenantId;
    protected String userId;
    protected Integer version;
    protected Long timestampMs;

    protected AbstractCommand(String tenantId, String userId, int version) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.version = version;
        timestampMs = Clock.systemUTC().millis();
    }

    @Override
    public String aggregateId() {
        return key(tenantId, userId);
    }

}
