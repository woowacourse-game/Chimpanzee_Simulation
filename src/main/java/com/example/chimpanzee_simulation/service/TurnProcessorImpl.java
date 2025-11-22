package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import org.springframework.stereotype.Service;

@Service
public class TurnProcessorImpl implements TurnProcessor {

    private final FoodProductionService foodProductionService;
    private final FoodConsumptionService foodConsumptionService;
    private final ReproductionService reproductionService;
    private final AgingService agingService;
    private final AlphaResolutionService alphaResolutionService;

    public TurnProcessorImpl(FoodProductionService foodProductionService,
                             FoodConsumptionService foodConsumptionService,
                             ReproductionService reproductionService,
                             AgingService agingService,
                             AlphaResolutionService alphaResolutionService) {
        this.foodProductionService = foodProductionService;
        this.foodConsumptionService = foodConsumptionService;
        this.reproductionService = reproductionService;
        this.agingService = agingService;
        this.alphaResolutionService = alphaResolutionService;
    }

    @Override
    public TurnResult runTurn(SimulationState state) {
        int currentTurn = state.turn();

        TurnLog log = new TurnLog(currentTurn);
        log.add("턴 " + currentTurn + " 처리 시작.");
        addAlphaSummary(state, log);

        // 0) 나이 증가 + 자연사 판정
        agingService.applyAgingAndNaturalDeath(state, log);
        // 1) 먹이 생산
        foodProductionService.produce(state, log);

        // 2) 알파 선출 & 도전 규칙
        alphaResolutionService.resolveAlpha(state, log);

        // 3) 먹이 소비 및 우선순위 기반 배분
        foodConsumptionService.consumeAndDistribute(state, log);

        // 4) 출산 처리 / 번식 처리
        reproductionService.process(state, log);

        state.nextTurn();

        return new TurnResult(state, log);
    }

    private void addAlphaSummary(SimulationState state, TurnLog log) {
        Chimpanzee alpha = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(Chimpanzee::alpha)
                .findFirst()
                .orElse(null);

        if (alpha == null) {
            log.add("현재 우두머리 없음");
            return;
        }

        String summary = "우두머리 침팬지: ID " + alpha.getId()
                + " (체력 " + alpha.health()
                + ", 힘 " + alpha.getStrength()
                + ", 민첩 " + alpha.getAgility() + ")";
        log.add(summary);
    }
}
