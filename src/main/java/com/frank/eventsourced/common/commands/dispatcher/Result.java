package com.frank.eventsourced.common.commands.dispatcher;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Operation result
 *
 * @author ftorriani
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Result {

    String status;
}
