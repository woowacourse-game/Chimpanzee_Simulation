// src/test/java/com/example/chimpanzee_simulation/service/FoodProductionServiceImplTest.java
package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FoodProductionServiceImplTest {

    Random random = new Random(123L); // seed 고정

    @Test
    void produce_shouldIncreaseFoodBasedOnWeatherAndDanger() {
        // living chimpanzee 20마리
        List<Chimpanzee> chimps = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            chimps.add(Chimpanzee.randomInitial((long) (i + 1), random, 0));
        }

        Environment env = Environment.of(0, Weather.SUNNY, 0.0);

        SimulationState state = new SimulationState(0, chimps, env, 123L);
        TurnLog log = new TurnLog(0);

        FoodProductionService service = new FoodProductionServiceImpl();

        int produced = service.produce(state, log);

        assertTrue(produced > 0, "SUNNY + dangerLevel 0 → 생산량은 0보다 커야 한다");
        assertEquals(produced, env.food());
    }
}
