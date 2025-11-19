package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationConfig;
import com.example.chimpanzee_simulation.domain.model.SimulationResult;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import org.springframework.stereotype.Service;

@Service
public class SimulationEngineImpl implements SimulationEngine {

    private final TurnProcessor turnProcessor;

    public SimulationEngineImpl(TurnProcessor turnProcessor) {
        this.turnProcessor = turnProcessor;
    }

    @Override
    public SimulationResult run(SimulationState initialState, SimulationConfig config) {
        SimulationState state = initialState;

        while (state.turn() < config.turnLimit()) {
            TurnResult result = turnProcessor.runTurn(state);
            state = result.nextState();
            // 나중에 여기서 턴 로그를 파일/콘솔로 출력하거나 수집 가능
        }

        int finalPopulation = state.chimpanzees().size();
        boolean extinction = finalPopulation == 0;

        return new SimulationResult(
                config.colonyName(),
                state.turn(),
                finalPopulation,
                extinction
        );
    }
}
