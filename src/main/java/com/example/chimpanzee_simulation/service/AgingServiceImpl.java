package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AgingServiceImpl implements AgingService {

    @Override
    public void applyAgingAndNaturalDeath(SimulationState state, TurnLog log) {
        int currentTurn = state.turn();
        Random random = state.random();

        for (Chimpanzee chimp : state.chimpanzees()) {
            if (!chimp.isAlive()) {
                continue;
            }

            // 나이 증가
            boolean aged = chimp.incrementAgeIfNeeded(currentTurn);
            if (aged) {
                // 연령 기반 스탯 성장/하락 적용
                chimp.applyAgeBasedGrowth(random);
                log.add("개체 #" + chimp.getId() + "의 나이가 1살 증가했습니다. (현재 나이: " + chimp.getAge() + ")");
            }

            // 자연사 판정
            boolean died = chimp.checkNaturalDeath(currentTurn, random);
            if (!died) {
                continue;
            }

            log.add("☠️ 개체 #" + chimp.getId() +
                    "가 노쇠로 자연사했습니다. (나이: " + chimp.getAge() +
                    ", 기대수명: " + chimp.getLongevity() +
                    ")");
        }
    }
}
