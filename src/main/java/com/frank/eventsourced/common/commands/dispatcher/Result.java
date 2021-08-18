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
 *
 * @deprecated AVRO event on their way
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Deprecated
public class Result {

    String status;
}
