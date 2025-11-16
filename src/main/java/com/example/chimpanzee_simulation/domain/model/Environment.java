package com.example.chimpanzee_simulation.domain.model;

import com.example.chimpanzee_simulation.domain.enums.Weather;

public class Environment {

    private int food;               // 한 턴에 생산된 먹이 수량
    private Weather weather;        // enum Weather { SUNNY, NORMAL, RAINY, STORM, DROUGHT}
    private double dangerLevel;     // 0.0 ~ 1.0 위험도
}
