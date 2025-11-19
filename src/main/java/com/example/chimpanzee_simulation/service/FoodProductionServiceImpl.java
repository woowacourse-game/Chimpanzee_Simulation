package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

@Service
public class FoodProductionServiceImpl implements FoodProductionService {

    private static final int BASE_NEED_PER_CHIMP = 5;
    private static final double BASE_PRODUCTION_FACTOR = 1.0;

    @Override
    public int produce(SimulationState state, TurnLog log) {

        int living = (int) state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .count();

        Environment env = state.environment();
        Weather weather = env.weather();
        double dangerLevel = env.dangerLevel();

        int baseTotalNeed = living * BASE_NEED_PER_CHIMP;
        double weatherMultiplier = weather.productionMultiplier();
        double dangerMultiplier = 1.0 - (dangerLevel * 0.5);

        double rawProduced = baseTotalNeed * BASE_PRODUCTION_FACTOR * weatherMultiplier * dangerMultiplier;
        int produced = (int) Math.round(rawProduced);

        if (produced < 0) produced = 0;

        env.addFood(produced);

        log.add("[FOOD_PRODUCTION] produced=" + produced
                + ", living=" + living
                + ", weather=" + weather
                + ", danger=" + dangerLevel);

        return produced;
    }
}
