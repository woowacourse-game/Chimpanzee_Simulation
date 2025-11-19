package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import org.springframework.stereotype.Service;

@Service
public class TurnProcessorImpl implements TurnProcessor {

    private final FoodProductionService foodProductionService;

    public TurnProcessorImpl(FoodProductionService foodProductionService) {
        this.foodProductionService = foodProductionService;
    }

    @Override
    public TurnResult runTurn(SimulationState state) {
        int currentTurn = state.turn();

        TurnLog log = new TurnLog(currentTurn);
        log.add("Turn " + currentTurn + " processed (stub).");

        // 나중에 여기 안에 환경/먹이/나이/건강/사망 룰이 들어감
        //먹이
        foodProductionService.produce(state, log);

        state.nextTurn();

        return new TurnResult(state, log);
    }
}
