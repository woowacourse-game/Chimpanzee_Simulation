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
    private final AgingService agingService;
    private final AlphaResolutionService alphaResolutionService;
    private final EnvironmentService environmentService;
    private final AlphaSummaryService alphaSummaryService;

    public TurnProcessorImpl(FoodProductionService foodProductionService,
                             FoodConsumptionService foodConsumptionService,
                             ReproductionService reproductionService,
                             AgingService agingService,
                             AlphaResolutionService alphaResolutionService,
                             EnvironmentService environmentService,
                             AlphaSummaryService alphaSummaryService) {
        this.foodProductionService = foodProductionService;
        this.foodConsumptionService = foodConsumptionService;
        this.reproductionService = reproductionService;
        this.agingService = agingService;
        this.alphaResolutionService = alphaResolutionService;
        this.environmentService = environmentService;
        this.alphaSummaryService = alphaSummaryService;
    }

    @Override
    public TurnResult runTurn(SimulationState state) {
        int currentTurn = state.turn();

        TurnLog log = new TurnLog(currentTurn);
        log.add("턴 " + currentTurn + " 처리 시작.");

        // 날씨 업데이트
        environmentService.updateWeatherAndDanger(state, log);
        // 알파 개체 정보 표시
        alphaSummaryService.logAlphaSummary(state, log);
        // 나이 증가 + 자연사 판정
        agingService.applyAgingAndNaturalDeath(state, log);
        // 먹이 생산
        foodProductionService.produce(state, log);
        // 알파 선출 & 도전 규칙
        alphaResolutionService.resolveAlpha(state, log);
        // 먹이 소비 및 우선순위 기반 배분
        foodConsumptionService.consumeAndDistribute(state, log);
        // 출산 처리 / 번식 처리
        reproductionService.process(state, log);

        state.nextTurn();

        return new TurnResult(state, log);
    }
}
