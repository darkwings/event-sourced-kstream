package com.frank.eventsourced.app.api;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

/**
 * @author ftorriani
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WidgetBean {

    String widgetId;
    int version;
    Map<String, String> data;
    Map<String, String> meta;
}
