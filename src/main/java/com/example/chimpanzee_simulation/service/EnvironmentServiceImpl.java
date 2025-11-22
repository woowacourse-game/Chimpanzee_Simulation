package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EnvironmentServiceImpl implements EnvironmentService {

    @Override
    public void updateWeatherAndDanger(SimulationState state, TurnLog log) {
        Environment env = state.environment();
        if (env == null) {
            return;
        }

        Weather before = env.weather();
        double dangerBefore = env.dangerLevel();

        Random random = state.random();
        Weather after = env.updateWeather(random);

        env.adjustDangerLevelForWeather(after);
        double dangerAfter = env.dangerLevel();

        if (before == after) {
            log.add("날씨 변화 없음 (현재: " + after + "), 위험도: "
                    + String.format("%.2f", dangerBefore) + " → " + String.format("%.2f", dangerAfter));
        } else {
            log.add("날씨 변화: " + before + " → " + after
                    + ", 위험도: " + String.format("%.2f", dangerBefore)
                    + " → " + String.format("%.2f", dangerAfter));
        }
    }
}

