// src/test/java/com/example/chimpanzee_simulation/service/FoodConsumptionServiceImplTest.java
package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FoodConsumptionServiceImplTest {

    /**
     * 케이스 1: 먹이가 충분한 경우
     * - 모든 개체가 기본 필요량만큼 먹고, health가 최대 +5까지 증가한다.
     * - 환경의 food는 개체 수 * 5 만큼 감소한다.
     */
    @Test
    void consumeAndDistribute_whenFoodIsEnough_allChimpsFedAndFoodDecreases() {
        // given
        int chimpCount = 10;
        Random random = new Random(123L);

        List<Chimpanzee> chimps = new ArrayList<>();
        for (int i = 0; i < chimpCount; i++) {
            chimps.add(Chimpanzee.randomInitial((long) (i + 1), random, 0));
        }

        // 먹이는 충분히 크게 잡는다 (개체 수 * 5 보다 훨씬 크게)
        int initialFood = 1000;
        Environment env = Environment.of(initialFood, Weather.NORMAL, 0.0);

        SimulationState state = new SimulationState(
                0,
                chimps,
                env,
                999L
        );
        TurnLog log = new TurnLog(0);

        FoodConsumptionService service = new FoodConsumptionServiceImpl();

        // health before snapshot
        List<Integer> beforeHealth = chimps.stream()
                .map(Chimpanzee::health)
                .toList();

        // when
        service.consumeAndDistribute(state, log);

        // then
        // 1) 환경 food 는 소비된 만큼 줄어야 한다 (chimpCount * 5 만큼 소비)
        int expectedConsumed = chimpCount * 5; // 전부 alpha=false 라는 가정 (randomInitial)
        assertEquals(initialFood - expectedConsumed, env.food());

        // 2) 각 침팬지의 health 는 0~5 까지만 증가, 100을 넘지 않는다
        for (int i = 0; i < chimpCount; i++) {
            int before = beforeHealth.get(i);
            int after = chimps.get(i).health();

            assertTrue(after >= before, "full-fed 상태에서는 health가 줄어들지 않아야 한다");
            assertTrue(after - before <= 5, "full-fed 보너스는 최대 +5 이하여야 한다");
            assertTrue(after <= 100, "health 상한은 100을 넘지 않아야 한다");
            assertTrue(chimps.get(i).alive(), "먹이가 충분한 상황에서는 아무도 굶어 죽지 않아야 한다");
        }
    }

    /**
     * 케이스 2: 먹이가 0인 경우
     * - 모든 개체가 굶주림 페널티(-10) 를 받는다.
     * - health ≤ 10 이었던 개체는 STARVATION 으로 사망 처리되어 alive == false 가 된다.
     * - health > 10 이었던 개체는 health 가 감소하지만 살아 있어야 한다.
     */
    @Test
    void consumeAndDistribute_whenFoodIsZero_healthDecreasesAndWeakChimpsDie() {
        // given
        int chimpCount = 20;
        Random random = new Random(456L);

        List<Chimpanzee> chimps = new ArrayList<>();
        for (int i = 0; i < chimpCount; i++) {
            chimps.add(Chimpanzee.randomInitial((long) (i + 1), random, 0));
        }

        // 먹이는 0 → 모두 굶주림 페널티를 받는 상황
        Environment env = Environment.of(0, Weather.NORMAL, 0.0);

        SimulationState state = new SimulationState(
                0,
                chimps,
                env,
                999L
        );
        TurnLog log = new TurnLog(0);

        FoodConsumptionService service = new FoodConsumptionServiceImpl();

        // health before snapshot
        List<Integer> beforeHealth = chimps.stream()
                .map(Chimpanzee::health)
                .toList();

        // when
        service.consumeAndDistribute(state, log);

        // then
        // 1) 환경 food 는 원래 0 이었으므로 여전히 0 이어야 한다
        assertEquals(0, env.food(), "먹이가 0인 상황에서는 소비 후에도 food는 0이어야 한다");

        // 2) health 변화 및 사망 여부 확인
        for (int i = 0; i < chimpCount; i++) {
            Chimpanzee chimp = chimps.get(i);
            int before = beforeHealth.get(i);
            int after = chimp.health();

            // health 는 최대 10까지만 줄어야 한다 (MAX_HUNGER_PENALTY = -10)
            assertTrue(before - after <= 10,
                    "굶주림 페널티는 최대 -10 이하여야 한다");

            if (before <= 10) {
                // 원래 health가 10 이하였다면, -10 페널티 후 health <= 0 → STARVATION 사망
                assertFalse(chimp.alive(),
                        "health가 10 이하였던 개체는 굶주림으로 사망해야 한다");
            } else {
                // 원래 health가 10 초과였다면, -10 후에도 0보다 크므로 살아 있어야 한다
                assertTrue(chimp.alive(),
                        "health가 10 초과였던 개체는 굶주림 페널티 후에도 살아 있어야 한다");
            }
        }
    }
}
