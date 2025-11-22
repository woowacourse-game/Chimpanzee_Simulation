package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;

public interface AlphaSummaryService {

    void logAlphaSummary(SimulationState state, TurnLog log);
}

