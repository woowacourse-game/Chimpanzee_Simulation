package com.example.chimpanzee_simulation.domain.service;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationInitializerImpl implements SimulationInitializer {

    private static final int DEFAULT_INITIAL_POPULATION = 20;

    private static final int MIN_LONGEVITY = 36;
    private static final int MAX_LONGEVITY = 45;

    private static final int INITIAL_FOOD = 100;

    private List<Chimpanzee> createInitialChimpanzees(int count, Random random) {
        List<Chimpanzee> chimpanzeeList = new ArrayList<>();

        for (int i = 0; i < count; i++) {

        }
    }
}
