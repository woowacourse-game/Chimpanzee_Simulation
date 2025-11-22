package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FoodConsumptionServiceImpl implements FoodConsumptionService {

    private static final int BASE_NEED_PER_CHIMP = 5;
    private static final double ALPHA_NEED_MULTIPLIER = 1.2;
    private static final int FULL_FED_HEALTH_BONUS = 5;
    private static final int MAX_HUNGER_PENALTY = -10;

    @Override
    public void consumeAndDistribute(SimulationState state, TurnLog log) {
        log.add("먹이 분배 규칙: 침팬지는 기본 5의 먹이가 필요하며, 우두머리는 약 6의 먹이가 필요합니다.");

        Environment env = state.environment();
        int foodBefore = env.food();
        int remainingFood = foodBefore;

        List<Chimpanzee> candidates = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .toList();

        if (candidates.isEmpty()) {
            log.add("먹이를 소비할 살아있는 침팬지가 없습니다. 먹이 분배를 건너뜁니다.");
            return;
        }

        List<Chimpanzee> sorted = candidates.stream()
                .sorted(Comparator.comparingInt(this::priorityLevel))
                .toList();

        List<Long> fullFed = new java.util.ArrayList<>();
        List<Long> underFed = new java.util.ArrayList<>();

        for (Chimpanzee chimp : sorted) {
            int need = individualNeed(chimp);

            if (need <= 0) {
                continue;
            }

            if (remainingFood <= 0) {
                chimp.applyHealthChange(MAX_HUNGER_PENALTY);
                handleStarvationDeathIfNeeded(chimp, log);

                underFed.add(chimp.getId());

                log.add("침팬지 ID " + chimp.getId() + "는 먹이를 전혀 먹지 못했습니다. (건강 " + MAX_HUNGER_PENALTY + " 감소)");
                continue;
            }

            if (remainingFood >= need) {
                // 충분히 먹을 수 있는 경우 (full-fed)
                int consumed = need;
                remainingFood -= consumed;
                env.consumeFood(consumed);

                chimp.applyHealthChange(FULL_FED_HEALTH_BONUS);

                fullFed.add(chimp.getId());

                log.add("침팬지 ID " + chimp.getId() + "는 필요한 양(" + need + ")을 모두 먹었습니다. (건강 +" + FULL_FED_HEALTH_BONUS + ")");
            } else {
                // 부분적으로만 먹는 경우 (partial-fed)
                int consumed = remainingFood;
                double fedRatio = consumed / (double) need;
                double hunger = 1.0 - fedRatio;

                // MAX_HUNGER_PENALTY는 음수이므로, 배고픔 비율만큼 스케일링
                int penalty = (int) Math.round(MAX_HUNGER_PENALTY * hunger);

                remainingFood = 0;
                env.consumeFood(consumed);

                chimp.applyHealthChange(penalty);
                handleStarvationDeathIfNeeded(chimp, log);

                underFed.add(chimp.getId());

                log.add("침팬지 ID " + chimp.getId() + "는 필요한 양(" + need + ") 중 " + consumed + "만 섭취했습니다. (건강 " + penalty + " 감소)");
            }
        }

        log.add("정상적으로 먹이를 섭취한 침팬지 ID: " + formatIds(fullFed));
        log.add("충분히 먹지 못한 침팬지 ID: " + formatIds(underFed));
        log.add("먹이 소비 요약: 턴 " + state.turn()
                + ", 소비 전=" + foodBefore
                + ", 소비 후=" + env.food());
    }

    private void handleStarvationDeathIfNeeded(Chimpanzee chimp, TurnLog log) {
        if (chimp.alive() && chimp.health() <= 0) {
            chimp.applyAliveAndDeathReason(DeathReason.STARVATION);
            log.add("침팬지 ID " + chimp.getId() + "가 굶주려 사망했습니다.");
        }
    }

    private int priorityLevel(Chimpanzee chimp) {
        AgeCategory ageCategory = chimp.ageCategory();
        Sex sex = chimp.sex();
        boolean alpha = chimp.alpha();

        // 1. 알파 성체 수컷
        if (alpha && sex == Sex.MALE && isAdult(ageCategory)) {
            return 1;
        }

        // 2. 일반 성체 수컷
        if (!alpha && sex == Sex.MALE && isAdult(ageCategory)) {
            return 2;
        }

        // (3. 임신/수유 암컷은 아직 모델에 없으므로 생략)

        // 4. 성체 암컷
        if (sex == Sex.FEMALE && isAdult(ageCategory)) {
            return 4;
        }

        // 5. 청소년/유년기
        if (ageCategory == AgeCategory.ADOLESCENT || ageCategory == AgeCategory.JUVENILE) {
            return 5;
        }

        // 6. 영아 + 노령
        if (ageCategory == AgeCategory.INFANT || ageCategory == AgeCategory.ELDER) {
            return 6;
        }

        // 기타: 가장 낮은 우선순위
        return 10;
    }

    private boolean isAdult(AgeCategory category) {
        return category == AgeCategory.YOUNG_ADULT || category == AgeCategory.ADULT;
    }

    /**
     * 개체별 필요량 계산
     */
    private int individualNeed(Chimpanzee chimp) {
        int need = BASE_NEED_PER_CHIMP;
        if (chimp.alpha()) {
            need = (int) Math.round(BASE_NEED_PER_CHIMP * ALPHA_NEED_MULTIPLIER);
        }
        return need;
    }

    private String formatIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return "없음";
        }
        return ids.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
