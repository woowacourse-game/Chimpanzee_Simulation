package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
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
        Environment env = state.environment();
        int foodBefore = env.food();
        int remainingFood = foodBefore;

        List<Chimpanzee> candidates = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .toList();

        if (candidates.isEmpty()) {
            log.add("[FOOD_CONSUMPTION] no alive chimpanzees, skip consumption");
            return;
        }

        List<Chimpanzee> sorted = candidates.stream()
                .sorted(Comparator.comparingInt(this::priorityLevel))
                .toList();

        // 3. 우선순위 순으로 먹이 배분
        for (Chimpanzee chimp : sorted) {
            int need = individualNeed(chimp);

            if (need <= 0) {
                continue;
            }

            if (remainingFood <= 0) {
                chimp.applyHealthChange(MAX_HUNGER_PENALTY);
                log.add("[FOOD_CONSUMPTION] chimp=" + chimpLogKey(chimp)
                        + ", need=" + need
                        + ", consumed=0"
                        + ", fedRatio=0.0"
                        + ", healthChange=" + MAX_HUNGER_PENALTY);
                continue;
            }

            if (remainingFood >= need) {
                // 충분히 먹을 수 있는 경우 (full-fed)
                int consumed = need;
                remainingFood -= consumed;
                env.consumeFood(consumed);

                chimp.applyHealthChange(FULL_FED_HEALTH_BONUS);

                log.add("[FOOD_CONSUMPTION] chimp=" + chimpLogKey(chimp)
                        + ", need=" + need
                        + ", consumed=" + consumed
                        + ", fedRatio=1.0"
                        + ", healthChange=+" + FULL_FED_HEALTH_BONUS);
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

                log.add("[FOOD_CONSUMPTION] chimp=" + chimpLogKey(chimp)
                        + ", need=" + need
                        + ", consumed=" + consumed
                        + ", fedRatio=" + String.format("%.2f", fedRatio)
                        + ", healthChange=" + penalty);
            }
        }

        log.add("[FOOD_CONSUMPTION] turn=" + state.turn()
                + ", foodBefore=" + foodBefore
                + ", foodAfter=" + env.food());
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

    /**
     * 로그용 키 – 아직 public id()가 없다면 hashCode 정도로 대체
     * 나중에 Chimpanzee에 id() 추가하면 교체 가능
     */
    private String chimpLogKey(Chimpanzee chimp) {
        return "@" + Integer.toHexString(System.identityHashCode(chimp));
    }
}
