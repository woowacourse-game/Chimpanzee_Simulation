package com.example.chimpanzee_simulation.domain.model;

public class SimulationConfig {

    private final String colonyName;
    private final int turnLimit;

    public SimulationConfig(String colonyName, int turnLimit) {
        this.colonyName = colonyName;
        this.turnLimit = turnLimit;
    }

    public String colonyName() {
        return colonyName;
    }

    public int turnLimit() {
        return turnLimit;
    }
}
