package com.example.chimpanzee_simulation.domain.enums;

public enum Weather {
    SUNNY,
    NORMAL,
    RAINY,
    STORM,
    DROUGHT;

    public double productionMultiplier() {
        return switch (this) {
            case SUNNY -> 1.3;
            case NORMAL -> 1.0;
            case RAINY -> 1.1;
            case STORM -> 0.7;
            case DROUGHT -> 0.4;
        };
    }
}
