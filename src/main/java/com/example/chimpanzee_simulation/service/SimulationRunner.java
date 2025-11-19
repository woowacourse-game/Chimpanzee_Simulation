package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationConfig;
import com.example.chimpanzee_simulation.domain.model.SimulationResult;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SimulationRunner implements CommandLineRunner {

    private final SimulationInitializer initializer;
    private final SimulationEngine engine;

    public SimulationRunner(SimulationInitializer initializer,
                            SimulationEngine engine) {
        this.initializer = initializer;
        this.engine = engine;
    }

    @Override
    public void run(String... args) {
        // 초기 상태 생성
        SimulationState initialState = initializer.createInitialState();

        // 시뮬레이션 설정
        SimulationConfig config = new SimulationConfig(
                "Default Colony",
                50
        );

        // 엔진 실행
        SimulationResult result = engine.run(initialState, config);

        // 요약 결과 출력
        System.out.println("===== Simulation Finished =====");
        System.out.println("Colony: " + result.colonyName());
        System.out.println("Total turns: " + result.totalTurns());
        System.out.println("Final population: " + result.finalPopulation());
        System.out.println("Extinction: " + result.extinction());
    }
}
