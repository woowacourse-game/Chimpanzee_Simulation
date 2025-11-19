package com.example.chimpanzee_simulation.domain.model;

public class TurnResult {

    private final SimulationState nextState;
    private final TurnLog turnLog;

    public TurnResult(SimulationState nextState, TurnLog turnLog) {
        this.nextState = nextState;
        this.turnLog = turnLog;
    }

    public SimulationState nextState() {
        return nextState;
    }

    public TurnLog turnLog() {
        return turnLog;
    }
}
