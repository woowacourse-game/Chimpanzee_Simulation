package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReproductionServiceImpl implements ReproductionService {

    private final Random random = new Random();
    private final AtomicLong idSequence = new AtomicLong(1L);

    @Override
    public void process(SimulationState state, TurnLog log) {
        int currentTurn = state.turn();

        // 출산 처리
        List<Chimpanzee> newborns = handleBirth(state, currentTurn, log);

        // 번식 처리 (짝짓기)
        handleMating(state, currentTurn, log);

        // 새로 태어난 개체들을 상태에 추가
        if (!newborns.isEmpty()) {
            state.chimpanzees().addAll(newborns);
        }
    }

    private List<Chimpanzee> handleBirth(SimulationState state, int currentTurn, TurnLog log) {
        List<Chimpanzee> newborns = new ArrayList<>();

        for (Chimpanzee mother : state.chimpanzees()) {
            if (!mother.isPregnant()) continue;
            if (mother.getPregnancyDueTurn() > currentTurn) continue;

            Long fatherId = mother.getPregnancyFatherId();

            if (fatherId == null) {
                mother.giveBirth();
                log.add("⚠️ 아버지 정보가 없어 출산을 진행하지 못했습니다. (어미:" + mother.getId() + ")");
                continue;
            }

            Optional<Chimpanzee> fatherOpt = findById(state, fatherId);
            if (fatherOpt.isEmpty()) {
                mother.giveBirth();
                log.add("⚠️ 아버지가 목록에서 사라져 출산에 실패했습니다. (아버지ID=" + fatherId + ")");
                continue;
            }

            Chimpanzee father = fatherOpt.get();
            Long childId = state.allocateChimpId();

            Chimpanzee child = Chimpanzee.createOffspring(
                    childId,
                    father,
                    mother,
                    currentTurn,
                    random
            );

            mother.giveBirth();
            newborns.add(child);

            log.add("\uD83D\uDC76 출산 완료! 어미: 개체#" + mother.getId() +
                    ", 아버지: 개체#" +father.getId() +
                    ", 자식: 개체#" + child.getId());
        }

        return newborns;
    }

    private void handleMating(SimulationState state, int currentTurn, TurnLog log) {
        List<Chimpanzee> males = new ArrayList<>();
        List<Chimpanzee> females = new ArrayList<>();

        for (Chimpanzee chimp : state.chimpanzees()) {
            if (!chimp.canMate()) continue;
            if (chimp.getSex() == Sex.MALE) {
                males.add(chimp);
                continue;
            }
            if (chimp.getSex() == Sex.FEMALE) {
                females.add(chimp);
            }
        }

        if (males.isEmpty() || females.isEmpty()) {
            log.add("⚠️이번 턴에는 번식 가능한 수컷 또는 암컷이 부족하여 짝짓기를 진행하지 못했습니다.");
            return;
        }

        Collections.shuffle(males, random);
        Collections.shuffle(females, random);

        int pairCount = Math.min(males.size(), females.size());

        for (int i = 0; i < pairCount; i++) {
            Chimpanzee male = males.get(i);
            Chimpanzee female = females.get(i);

            if (female.isPregnant()) continue;

            double prob = (male.getReproductionRate() + female.getReproductionRate()) / 2.0;
            double r = random.nextDouble();

            if (r >= prob) {
                log.add("💔 짝짓기 실패... 수컷: 개체#" + male.getId()
                        + ", 암컷: 개체" + female.getId()
                        + ", 성공확률:" + String.format("%.0f%%", prob * 100));
                continue;
            }

            female.conceive(currentTurn, male.getId());

            log.add("❤️ 짝짓기 성공! 수컷: 개체" + male.getId()
                    + ", 암컷: 개체" + female.getId()
                    + ", 성공확률:" + String.format("%.0f%%", prob * 100));
        }
    }

    private Optional<Chimpanzee> findById(SimulationState state, Long id) {
        return state.chimpanzees().stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst();
    }
}
