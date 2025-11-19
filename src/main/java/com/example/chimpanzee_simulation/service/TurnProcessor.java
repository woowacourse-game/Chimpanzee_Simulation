package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnResult;

public interface TurnProcessor {

    TurnResult runTurn(SimulationState state);
}
