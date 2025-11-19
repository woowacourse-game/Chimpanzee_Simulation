package com.example.chimpanzee_simulation.domain.model;

import java.util.List;

public class SimulationState {

    private int turnNumber;                     // 현재 턴 번호
    private List<Chimpanzee> chimpanzees;
    private Environment environment;

    private long randomSeed;  // 재현성을 위해 필요하면 사용

    public SimulationState(int turnNumber, List<Chimpanzee> chimpanzees, Environment environment, long randomSeed) {
        this.turnNumber = turnNumber;
        this.chimpanzees = chimpanzees;
        this.environment = environment;
        this.randomSeed = randomSeed;
    }

    // 테스트용 getter
    int getTurnNumber() {
        return turnNumber;
    }

    List<Chimpanzee> getChimpanzees() {
        return chimpanzees;
    }

    Environment getEnvironment() {
        return environment;
    }
}
