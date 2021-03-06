package com.frank.eventsourced.app.commands.beans;

import com.frank.eventsourced.common.commands.beans.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.frank.eventsourced.app.utils.KeyBuilder.key;

/**
 * @author ftorriani
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AddWidgetCommand implements Command {

    String tenantId;
    String userId;
    String widgetId;
    Map<String, String> meta;
    Map<String, String> data;
    int version;

    @Override
    public String aggregateId() {
        return key( tenantId, userId );
    }

    @Override
    public int expectedVersion() {
        return version;
    }
}