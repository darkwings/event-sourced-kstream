package com.frank.eventsourced.app.commands.beans;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * @author ftorriani
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class AddWidgetCommand extends AbstractCommand {

    String widgetId;
    Map<String, String> meta;
    Map<String, String> data;

    @Builder
    public AddWidgetCommand(String tenantId, String userId, Integer version, String widgetId,
                            Map<String, String> meta, Map<String, String> data) {
        super(tenantId, userId, version);
        this.widgetId = widgetId;
        this.meta = meta;
        this.data = data;
    }
}