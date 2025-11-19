package com.example.chimpanzee_simulation.domain.model;

public class SimulationResult {

    private final String colonyName;
    private final int totalTurns;
    private final int finalPopulation;
    private final boolean extinction;

    public SimulationResult(String colonyName,
                            int totalTurns,
                            int finalPopulation,
                            boolean extinction) {
        this.colonyName = colonyName;
        this.totalTurns = totalTurns;
        this.finalPopulation = finalPopulation;
        this.extinction = extinction;
    }

    public String colonyName() {
        return colonyName;
    }

    public int totalTurns() {
        return totalTurns;
    }

    public int finalPopulation() {
        return finalPopulation;
    }

    public boolean extinction() {
        return extinction;
    }
}
