package com.frank.eventsourced.app.commands.beans;

import com.frank.eventsourced.app.utils.KeyBuilder;
import com.frank.eventsourced.common.commands.beans.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.frank.eventsourced.app.utils.KeyBuilder.key;

/**
 * @author ftorriani
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CreateAppCommand implements Command {

    String tenantId;
    String userId;
    int version;

    public String aggregateId() {
        return key( tenantId, userId );
    }

    @Override
    public int expectedVersion() {
        return version;
    }
}