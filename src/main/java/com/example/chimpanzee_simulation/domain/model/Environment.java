package com.example.chimpanzee_simulation.domain.model;

import com.example.chimpanzee_simulation.domain.enums.Weather;

import java.util.Random;

public class Environment {

    private int food;               // 한 턴에 생산된 먹이 수량
    private Weather weather;        // enum Weather { SUNNY, NORMAL, RAINY, STORM, DROUGHT}
    private double dangerLevel; // 0.0 ~ 1.0 위험도

    private Environment(int food, Weather weather, double dangerLevel) {
        this.food = food;
        this.weather = weather;
        this.dangerLevel = dangerLevel;
    }

    public static Environment of(int food, Weather weather, double dangerLevel) {
        return new Environment(food, weather, dangerLevel);
    }


    public static Environment randomInitial(int count, Random random) {
        int food = count * 5;

        Weather[] weathers = Weather.values();
        int idx = random.nextInt(weathers.length);
        Weather weather = weathers[idx];

        double dangerLevel = random.nextDouble() * 0.5;

        return new Environment(food, weather, dangerLevel);
    }

    // 테스트용 getter
    int getFood() {
        return food;
    }

    Weather getWeather() {
        return weather;
    }

    double getDangerLevel() {
        return dangerLevel;
    }

    public Weather weather() {
        return weather;
    }

    public double dangerLevel() {
        return dangerLevel;
    }

    public int  food() {
        return food;
    }

    public void addFood(int amount) {
        if (amount <= 0) {
            return;
        }
        this.food += amount;
    }
}
