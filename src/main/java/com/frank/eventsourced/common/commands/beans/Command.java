package com.frank.eventsourced.common.commands.beans;

/**
 * @author ftorriani
 */
public interface Command {

    String aggregateId();

    int expectedVersion();
}