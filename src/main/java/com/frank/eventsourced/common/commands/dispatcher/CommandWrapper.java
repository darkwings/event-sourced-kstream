package com.frank.eventsourced.common.commands.dispatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ftorriani
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CommandWrapper {

    private String type;
    private String json;
}
