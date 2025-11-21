package com.example.chimpanzee_simulation.service;
import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AgingServiceImplTest {

    // 고정된 값을 돌려주는 Random (자연사 확률 테스트용)
    static class FixedRandom extends Random {
        private final double value;

        FixedRandom(double value) {
            this.value = value;
        }

        @Override
        public double nextDouble() {
            return value;
        }
    }

    @Test
    @DisplayName("birthTurn 기준으로 4턴마다 나이가 1씩 증가한다")
    void incrementAge_every_four_turns() {
        // given
        Chimpanzee chimp = Chimpanzee.ofFull(
                1L,
                0,                  // age
                Sex.MALE,
                100,                // health
                50,                 // strength
                50,                 // agility
                0.5,                // reproductionRate
                40,                 // longevity
                false,              // alpha
                true,               // alive
                DeathReason.NONE,
                AgeCategory.INFANT, // 초기 카테고리
                0,                  // birthTurn
                false,              // pregnant
                -1,
                null
        );

        int currentTurn = 4; // diff = 4 - 0 = 4 → 4턴 경과

        // when
        boolean aged = chimp.incrementAgeIfNeeded(currentTurn);

        // then
        assertTrue(aged);
        assertEquals(1, chimp.getAge());
        assertEquals(AgeCategory.INFANT, chimp.getAgeCategory()); // 1살이면 여전히 INFANT
    }

    @Test
    @DisplayName("4의 배수가 아닌 턴에는 나이가 증가하지 않는다")
    void incrementAge_does_not_change_on_non_multiple_of_four() {
        // given
        Chimpanzee chimp = Chimpanzee.ofFull(
                2L,
                2,                  // age
                Sex.FEMALE,
                100,
                50,
                50,
                0.5,
                40,
                false,
                true,
                DeathReason.NONE,
                AgeCategory.JUVENILE,
                0,                  // birthTurn
                false,
                -1,
                null
        );

        int currentTurn = 3; // diff = 3 → 4의 배수 아님

        // when
        boolean aged = chimp.incrementAgeIfNeeded(currentTurn);

        // then
        assertFalse(aged);
        assertEquals(2, chimp.getAge());
        assertEquals(AgeCategory.JUVENILE, chimp.getAgeCategory());
    }

    @Test
    @DisplayName("기대수명을 넘긴 상태에서 r < base 이면 자연사(OLD_AGE) 처리된다")
    void naturalDeath_occurs_when_random_below_base() {
        // given
        int age = 12;
        int longevity = 10;
        // base = 0.1 * (age - longevity + 1) = 0.1 * (12 - 10 + 1) = 0.3

        Chimpanzee chimp = Chimpanzee.ofFull(
                4L,
                age,
                Sex.FEMALE,
                100,
                50,
                50,
                0.5,
                longevity,
                false,
                true,
                DeathReason.NONE,
                AgeCategory.ADULT,
                0,
                false,
                -1,
                null
        );

        Random random = new FixedRandom(0.1); // r = 0.1 < base(0.3) → 죽어야 함

        // when
        boolean died = chimp.checkNaturalDeath(1, random);

        // then
        assertTrue(died);
        assertFalse(chimp.isAlive());
        assertEquals(DeathReason.OLD_AGE, chimp.getDeathReason());
    }

}
