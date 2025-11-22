package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

@Service
public class AlphaSummaryServiceImpl implements AlphaSummaryService {

    @Override
    public void logAlphaSummary(SimulationState state, TurnLog log) {
        Chimpanzee alpha = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(Chimpanzee::alpha)
                .findFirst()
                .orElse(null);

        if (alpha == null) {
            log.add("현재 우두머리 없음");
            return;
        }

        String summary = "우두머리 침팬지: ID " + alpha.getId()
                + " (체력 " + alpha.health()
                + ", 힘 " + alpha.getStrength()
                + ", 민첩 " + alpha.getAgility() + ")";
        log.add(summary);
    }
}

