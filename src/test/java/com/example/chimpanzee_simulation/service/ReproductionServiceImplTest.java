package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ReproductionServiceImplTest {

    @Test
    @DisplayName("성체이고 건강 >= 50, 살아 있고 임신 중이 아니면 짝짓기 가능(canMate = true)")
    void canMate_true_when_adult_healthy_notPregnant_alive() {
        // given
        Chimpanzee male = Chimpanzee.ofFull(
                1L,
                20,                 // age
                Sex.MALE,
                80,                 // health >= 50
                70,                 // strength
                70,                 // agility
                0.7,
                40,
                false,              // alpha
                true,               // alive
                DeathReason.NONE,
                AgeCategory.ADULT,
                0,                  // birthTurn
                false,              // pregnant
                -1,
                null
        );

        // when
        boolean result = male.canMate();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("건강이 50 미만이면 짝짓기 불가능(canMate = false)")
    void canMate_false_when_health_too_low() {
        // given
        Chimpanzee female = Chimpanzee.ofFull(
                2L,
                20,
                Sex.FEMALE,
                30,                 // health < 50
                70,
                70,
                0.7,
                40,
                false,
                true,
                DeathReason.NONE,
                AgeCategory.ADULT,
                0,
                false,
                -1,
                null
        );

        // when
        boolean result = female.canMate();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("임신 상태면 짝짓기 불가능")
    void canMate_false_when_pregnant() {
        Chimpanzee mother = Chimpanzee.ofFull(
                3L,
                20,
                Sex.FEMALE,
                100,
                70,
                70,
                0.8,
                40,
                false,
                true,
                DeathReason.NONE,
                AgeCategory.ADULT,
                0,
                true,       // pregnant
                10,
                99L
        );

        assertFalse(mother.canMate());
    }

    @Test
    @DisplayName("giveBirth() 호출 시 임신 상태 초기화")
    void giveBirth_resets_pregnancy_state() {
        Chimpanzee mother = Chimpanzee.ofFull(
                6L,
                20,
                Sex.FEMALE,
                100,
                70,
                70,
                0.8,
                40,
                false,
                true,
                DeathReason.NONE,
                AgeCategory.ADULT,
                0,
                true,       // pregnant
                15,
                1L
        );

        mother.giveBirth();

        assertFalse(mother.isPregnant());
        assertEquals(-1, mother.getPregnancyDueTurn());
        assertNull(mother.getPregnancyFatherId());
    }
}
