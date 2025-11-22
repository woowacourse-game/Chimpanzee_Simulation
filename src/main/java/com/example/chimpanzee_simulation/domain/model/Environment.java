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

        // 초기 위험도: 0.3 ~ 0.7 구간에서 시작
        double dangerLevel = 0.3 + random.nextDouble() * 0.4;

        return new Environment(food, weather, dangerLevel);
    }

    /**
     * 현재 날씨를 기반으로 다음 턴의 날씨를 결정하는 규칙.
     */
    public Weather updateWeather(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }

        Weather current = (weather != null) ? weather : Weather.NORMAL;
        Weather next = switch (current) {
            case SUNNY -> pickNextWeather(
                    random,
                    new Weather[]{Weather.SUNNY, Weather.NORMAL, Weather.RAINY, Weather.STORM, Weather.DROUGHT},
                    new double[]{0.5, 0.3, 0.1, 0.05, 0.05}
            );
            case NORMAL -> pickNextWeather(
                    random,
                    new Weather[]{Weather.NORMAL, Weather.SUNNY, Weather.RAINY, Weather.STORM, Weather.DROUGHT},
                    new double[]{0.4, 0.2, 0.2, 0.1, 0.1}
            );
            case RAINY -> pickNextWeather(
                    random,
                    new Weather[]{Weather.RAINY, Weather.NORMAL, Weather.STORM, Weather.SUNNY},
                    new double[]{0.4, 0.3, 0.2, 0.1}
            );
            case STORM -> pickNextWeather(
                    random,
                    new Weather[]{Weather.STORM, Weather.RAINY, Weather.NORMAL},
                    new double[]{0.3, 0.4, 0.3}
            );
            case DROUGHT -> pickNextWeather(
                    random,
                    new Weather[]{Weather.DROUGHT, Weather.SUNNY, Weather.NORMAL},
                    new double[]{0.4, 0.3, 0.3}
            );
        };

        this.weather = next;
        return next;
    }

    /**
     * 현재 날씨에 따른 목표 위험도(target)에 맞추어
     * dangerLevel을 부드럽게 보정한다.
     *
     * 예) dangerLevel = 0.8 * dangerLevel + 0.2 * target
     */
    public void adjustDangerLevelForWeather(Weather currentWeather) {
        double target = switch (currentWeather) {
            case SUNNY -> 0.1;
            case NORMAL -> 0.2;
            case RAINY -> 0.25;
            case STORM -> 0.6;
            case DROUGHT -> 0.7;
        };

        double updated = (this.dangerLevel * 0.8) + (target * 0.2);
        if (updated < 0.0) {
            updated = 0.0;
        } else if (updated > 1.0) {
            updated = 1.0;
        }
        this.dangerLevel = updated;
    }

    private static Weather pickNextWeather(Random random, Weather[] candidates, double[] probabilities) {
        if (candidates.length != probabilities.length) {
            throw new IllegalArgumentException("candidates and probabilities must have same length");
        }

        double r = random.nextDouble();
        double cumulative = 0.0;

        for (int i = 0; i < candidates.length; i++) {
            cumulative += probabilities[i];
            if (r < cumulative) {
                return candidates[i];
            }
        }

        // 부동소수점 오차 등을 대비해 마지막 후보를 반환
        return candidates[candidates.length - 1];
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

    public void consumeFood(int amount) {
        if (amount <= 0) {
            return;
        }
        if (amount > this.food) {
            amount = this.food;
        }
        this.food -= amount;
    }
}
