package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import org.springframework.stereotype.Service;

@Service
public class TurnProcessorImpl implements TurnProcessor {

    private final FoodProductionService foodProductionService;
    private final FoodConsumptionService foodConsumptionService;
    private final ReproductionService reproductionService;
    private final AlphaResolutionService alphaResolutionService;

    public TurnProcessorImpl(FoodProductionService foodProductionService,
                             FoodConsumptionService foodConsumptionService,
                             ReproductionService reproductionService,
                             AlphaResolutionService alphaResolutionService) {
        this.foodProductionService = foodProductionService;
        this.foodConsumptionService = foodConsumptionService;
        this.reproductionService = reproductionService;
        this.alphaResolutionService = alphaResolutionService;
    }

    @Override
    public TurnResult runTurn(SimulationState state) {
        int currentTurn = state.turn();

        TurnLog log = new TurnLog(currentTurn);
        log.add("Turn " + currentTurn + " processed (stub).");

        // 나중에 여기 안에 환경/먹이/나이/건강/사망 룰이 들어감
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
}
