package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodProductionServiceImplTest {

    SimulationInitializer simulationInitializer = new SimulationInitializerImpl();

    @Test
    void produce_shouldIncreaseFoodBasedOnWeatherAndDanger() {

        SimulationState initialState = simulationInitializer.createInitialState();

        TurnLog log = new TurnLog(0);

        FoodProductionService service = new FoodProductionServiceImpl();

        int produced = service.produce(initialState, log);

        assertTrue(produced > 0, "SUNNY + dangerLevel 0 → 생산량은 0보다 커야 한다");
    }
}
