package com.example.chimpanzee_simulation.domain.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;

public interface SimulationInitializer {

    SimulationState createInitialState();
}
