package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.*;
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
            printTurnLog(result.turnLog());
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
    private void printTurnLog(TurnLog log) {
        System.out.println("---------- Turn " + log.turn() + " ----------");
        for (String msg : log.messages()) {
            System.out.println(msg);
        }
        System.out.println();
    }
}
