package com.frank.eventsourced.app.commands.beans;

import com.frank.eventsourced.common.commands.beans.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;

import static com.frank.eventsourced.app.utils.KeyBuilder.key;

/**
 * @author ftorriani
 */
@AllArgsConstructor
@Builder
@Getter
public class CreateAppCommand implements Command {

    String tenantId;
    String userId;
    int version;
    long timestampMs;

    private CreateAppCommand() {
        timestampMs = Clock.systemUTC().millis();
    }

    public String aggregateId() {
        return key(tenantId, userId);
    }

    @Override
    public int expectedVersion() {
        return version;
    }

    @Override
    public long timestampMs() {
        return timestampMs;
    }
}