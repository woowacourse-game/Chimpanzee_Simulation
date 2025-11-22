package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class SimulationEngineImpl implements SimulationEngine {

    private final TurnProcessor turnProcessor;
    private final Scanner scanner = new Scanner(System.in);

    public SimulationEngineImpl(TurnProcessor turnProcessor) {
        this.turnProcessor = turnProcessor;
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
            if (userQuit()) {
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

    private boolean userQuit() {
        System.out.println("[s: 다음 턴 진행 / q: 종료]");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("q")){
            return true;
        }

        while (!input.equalsIgnoreCase("s")) {
            System.out.print("[s: 다음 턴 진행 / q: 종료] > ");
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                return true;
            }
        }
        return false;
    }

    private void printTurnLog(TurnLog log) {
        System.out.println("---------- Turn " + log.turn() + " ----------");
        for (String msg : log.messages()) {
            System.out.println(msg);
        }
        System.out.println();
    }
}
