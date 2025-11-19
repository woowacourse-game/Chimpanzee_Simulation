package com.example.chimpanzee_simulation.service;


import com.example.chimpanzee_simulation.domain.model.SimulationConfig;
import com.example.chimpanzee_simulation.domain.model.SimulationResult;
import com.example.chimpanzee_simulation.domain.model.SimulationState;

public interface SimulationEngine {

    SimulationResult run(SimulationState initialState, SimulationConfig config);
}
