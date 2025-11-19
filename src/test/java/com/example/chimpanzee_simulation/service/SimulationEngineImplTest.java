package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationConfig;
import com.example.chimpanzee_simulation.domain.model.SimulationResult;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineImplTest {


    @Test
    void run_shouldStopAtTurnLimit_andReturnResult() {
        // given
        SimulationState initialState = new SimulationState(
                0,
                List.of(),     // 빈 침팬지 리스트
                null,
                123L
        );

        SimulationConfig config = new SimulationConfig(
                "Test Colony",
                5
        );

        TurnProcessor stubTurnProcessor = state -> {
            int currentTurn = state.turn();
            TurnLog log = new TurnLog(currentTurn);
            log.add("stub turn " + currentTurn);
            state.nextTurn();
            return new TurnResult(state, log);
        };

        SimulationEngine engine = new SimulationEngineImpl(stubTurnProcessor);

        // when
        SimulationResult result = engine.run(initialState, config);

        // then
        assertEquals("Test Colony", result.colonyName());
        assertEquals(5, result.totalTurns());
        // 빈 리스트로 시작했으니 최종 개체 수는 0
        assertEquals(0, result.finalPopulation());
        assertTrue(result.extinction(), "초기 개체 수가 0이므로 멸종 상태로 간주해야 한다.");
    }

}
