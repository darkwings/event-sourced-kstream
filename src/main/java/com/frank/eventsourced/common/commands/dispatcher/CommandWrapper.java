package com.frank.eventsourced.common.commands.dispatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ftorriani
 *
 * @deprecated AVRO event on their way
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Deprecated
public class CommandWrapper {

    private String type;
    private String json;
}
