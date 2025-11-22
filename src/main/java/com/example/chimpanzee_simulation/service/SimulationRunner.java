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
                "기본 생태계",
                Integer.MAX_VALUE
        );

        // 엔진 실행
        SimulationResult result = engine.run(initialState, config);

        // 요약 결과 출력
        System.out.println("===== 🐒 시뮬레이션 종료 🐒 =====");
        System.out.println("🌴 무리 이름: " + result.colonyName());
        System.out.println("⏱️ 총 진행 턴: " + result.totalTurns() + "턴");
        System.out.println("👥 최종 개체 수: " + result.finalPopulation() + "마리");
        System.out.print("💀 전멸 여부: ");
        if (result.extinction()) {
            System.out.println("예");
        } else {
            System.out.println("아니오");
        }
    }
}
