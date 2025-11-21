package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class AlphaResolutionServiceImpl implements AlphaResolutionService {

    @Override
    public void resolveAlpha(SimulationState state, TurnLog log) {

        Chimpanzee currentAlpha = findCurrentAlpha(state);

        if (currentAlpha == null) {
            electAlpha(state, log);
            return;
        }
    }

    private Chimpanzee findCurrentAlpha(SimulationState state) {
        return state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(Chimpanzee::alpha)
                .findFirst()
                .orElse(null);
    }

    /**
     * 알파가 없을 때 새로운 알파를 선출한다.
     * 후보 조건:
     * - alive == true
     * - sex == MALE
     * - ageCategory == YOUNG_ADULT or ADULT
     * - health >= 60
     */
    private void electAlpha(SimulationState state, TurnLog log) {

        Random random = state.random();

        List<Chimpanzee> candidates = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(c -> c.sex() == Sex.MALE)
                .filter(c -> c.ageCategory() == AgeCategory.YOUNG_ADULT || c.ageCategory() == AgeCategory.ADULT)
                .filter(c -> c.health() >= 60)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            log.add("[ALPHA] No eligible candidates. Alpha remains empty.");
            return;
        }

        // 점수 계산
        List<ChimpanzeeScore> scored = candidates.stream()
                .map(c -> new ChimpanzeeScore(c, c.strength() * 0.7 + c.agility() * 0.3))
                .collect(Collectors.toList());

        double maxScore = scored.stream()
                .mapToDouble(s -> s.score)
                .max()
                .orElse(0.0);

        List<ChimpanzeeScore> top = scored.stream()
                .filter(s -> s.score == maxScore)
                .collect(Collectors.toList());

        // 동점 시 랜덤 선택
        ChimpanzeeScore winner = top.get(random.nextInt(top.size()));

        winner.chimp.assignAlpha();

        log.add(String.format("[ALPHA] New alpha elected: id=%d (score=%.2f)",
                winner.chimp.getId(), winner.score));
    }

    private static class ChimpanzeeScore {
        Chimpanzee chimp;
        double score;

        ChimpanzeeScore(Chimpanzee c, double score) {
            this.chimp = c;
            this.score = score;
        }
    }
}
