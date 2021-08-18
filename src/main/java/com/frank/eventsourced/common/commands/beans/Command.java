package com.frank.eventsourced.common.commands.beans;

/**
 * @author ftorriani
 *
 * @deprecated AVRO event on their way....
 */
@Deprecated
public interface Command {

    String aggregateId();

    Integer getVersion();

    Long getTimestampMs();
}