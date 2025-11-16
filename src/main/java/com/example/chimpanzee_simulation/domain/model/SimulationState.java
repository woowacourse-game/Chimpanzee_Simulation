package com.example.chimpanzee_simulation.domain.model;

import java.util.List;

public class SimulationState {

    private int turnNumber;                     // 현재 턴 번호
    private List<Chimpanzee> chimpanzees;
    private Environment environment;

    private long randomSeed;        // 재현성을 위해 필요하면 사용
}
