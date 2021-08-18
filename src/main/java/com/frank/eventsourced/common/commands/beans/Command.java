package com.frank.eventsourced.common.commands.beans;

/**
 * @author ftorriani
 */
public interface Command {

    String aggregateId();

    Integer getVersion();

    Long getTimestampMs();
}