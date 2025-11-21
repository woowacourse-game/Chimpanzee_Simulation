package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;

public interface ReproductionService {

    void process(SimulationState state, TurnLog log);
}
