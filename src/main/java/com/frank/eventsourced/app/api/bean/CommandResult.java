package com.frank.eventsourced.app.api.bean;

import lombok.Value;

@Value
public class CommandResult {

    public enum Outcome {
        OK, KO
    }
    Outcome outcome;
    String operationId;
}
