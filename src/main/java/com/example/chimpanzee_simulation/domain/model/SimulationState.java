package com.example.chimpanzee_simulation.domain.model;

import java.util.List;
import java.util.Random;

public class SimulationState {

    private int turnNumber;                     // 현재 턴 번호
    private List<Chimpanzee> chimpanzees;
    private Environment environment;

    private Long randomSeed;  // 재현성을 위해 필요하면 사용
    private final Random random;

    public SimulationState(int turnNumber, List<Chimpanzee> chimpanzees, Environment environment, long randomSeed) {
        this.turnNumber = turnNumber;
        this.chimpanzees = chimpanzees;
        this.environment = environment;
        this.randomSeed = randomSeed;
        this.random = new Random(randomSeed);
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

    public long randomSeed() {
        return randomSeed;
    }

    public Random random() {
        return random;
    }

    public void nextTurn() {
        this.turnNumber += 1;
    }

    public int turn() {
        return turnNumber;
    }

    public List<Chimpanzee> chimpanzees() {
        return chimpanzees;
    }

    public Environment environment() {
        return environment;
    }
}
