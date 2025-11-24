package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationConfig;
import com.example.chimpanzee_simulation.domain.model.SimulationResult;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import com.example.chimpanzee_simulation.domain.model.TurnResult;
import com.example.chimpanzee_simulation.service.ui.ChimpanzeeTableScreen;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineImplTest {


    @Test
    void run_shouldStopWhenAllDead_andReturnResult() {
        // given
        SimulationState initialState = new SimulationState(
                0,
                List.of(),
                null,
                123L
        );
        SimulationConfig config = new SimulationConfig("Test Colony", 5);

        TurnProcessor stubTurnProcessor = state -> {
            int currentTurn = state.turn();
            TurnLog log = new TurnLog(currentTurn);
            log.add("stub turn " + currentTurn);
            state.nextTurn();
            return new TurnResult(state, log);
        };

        // ★ ChimpanzeeTableScreen 모의 객체 주입
        ChimpanzeeTableScreen dummyScreen = Mockito.mock(ChimpanzeeTableScreen.class);

        // 생성자 시그니처가 (TurnProcessor, ChimpanzeeTableScreen) 인 경우
        SimulationEngine engine = new SimulationEngineImpl(stubTurnProcessor, dummyScreen);

        // when
        SimulationResult result = engine.run(initialState, config);

        // then
        assertEquals("Test Colony", result.colonyName());
        // 현재 엔진이 '모두 사망 시 즉시 종료'라면 1턴에서 멈춤
        assertEquals(1, result.totalTurns());
        assertEquals(0, result.finalPopulation());
        assertTrue(result.extinction());
    }

}
