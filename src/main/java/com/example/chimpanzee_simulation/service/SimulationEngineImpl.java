package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.*;
import com.example.chimpanzee_simulation.service.ui.ChimpanzeeTableScreen;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class SimulationEngineImpl implements SimulationEngine {

    private final TurnProcessor turnProcessor;
    private final ChimpanzeeTableScreen tableScreen;
    private final Scanner scanner = new Scanner(System.in);

    public SimulationEngineImpl(TurnProcessor turnProcessor,
                                ChimpanzeeTableScreen tableScreen) {
        this.turnProcessor = turnProcessor;
        this.tableScreen = tableScreen;
    }

    @Override
    public SimulationResult run(SimulationState initialState, SimulationConfig config) {
        SimulationState state = initialState;

        while (true) {
            // 1. 한 턴 실행
            TurnResult result = turnProcessor.runTurn(state);
            state = result.nextState();

            // 2. 턴 로그 출력
            printTurnLog(result.turnLog());

            // 3. 종료 조건 1: 모든 개체 사망
            boolean allDead = state.chimpanzees()
                    .stream()
                    .noneMatch(Chimpanzee::alive);

            if (allDead) {
                System.out.println("모든 침팬지가 사망했습니다. 시뮬레이션이 종료되었습니다.");
                break;
            }

            // 4. 종료 조건 2: 사용자 종료 입력
            if (handleMenu(state)) {
                System.out.println("사용자 종료(q) 입력으로 시뮬레이션이 종료되었습니다.");
                break;
            }
        }
        return new SimulationResult(
                config.colonyName(),
                state.turn(),
                state.chimpanzees().size(),
                state.chimpanzees().stream().noneMatch(Chimpanzee::isAlive)
        );
    }

    private boolean handleMenu(SimulationState state) {
        System.out.println("[s: 다음 턴 진행 / c: 침팬지 현황표 / q: 종료]");
        System.out.print("> ");
        String input = scanner.nextLine().trim().toLowerCase();

        if ("q".equals(input)) return true;           // 종료
        if ("c".equals(input)) {                      // 표 화면 진입
            tableScreen.open(state);                  // ALT 화면에서 표 렌더 후, s 로 복귀
            return false;                             // 복귀하면 다음 입력/턴으로
        }
        while (!"s".equals(input)) {                  // 's'가 나올 때까지 반복
            System.out.print("[s: 다음 / c: 표 / q: 종료] > ");
            input = scanner.nextLine().trim().toLowerCase();
            if ("q".equals(input)) return true;
            if ("c".equals(input)) {
                tableScreen.open(state);
                // 표 화면에서 's' 누르면 원래 로그 화면으로 복귀됨
                // 계속 입력 대기
            }
        }
        return false; // 's' → 다음 턴
    }

    private void printTurnLog(TurnLog log) {
        System.out.println("---------- Turn " + log.turn() + " ----------");
        for (String msg : log.messages()) {
            System.out.println(msg);
        }
        System.out.println();
    }
}
