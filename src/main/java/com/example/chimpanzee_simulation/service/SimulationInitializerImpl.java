package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SimulationInitializerImpl implements SimulationInitializer {

    private static final int DEFAULT_INITIAL_POPULATION = 20;

    @Override
    public SimulationState createInitialState() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);

        List<Chimpanzee> chimpanzees = new ArrayList<>();
        for (int i =0; i < DEFAULT_INITIAL_POPULATION; i++) {
            chimpanzees.add(Chimpanzee.randomInitial((long) i+1, random,0));
        }

        Environment environment = Environment.randomInitial(DEFAULT_INITIAL_POPULATION, random);

        SimulationState state = new SimulationState(
                0,
                chimpanzees,
                environment,
                seed
        );

        return state;
    }
}
