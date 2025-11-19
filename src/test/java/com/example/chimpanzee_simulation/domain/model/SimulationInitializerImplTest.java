package com.example.chimpanzee_simulation.domain.model;
import static org.junit.jupiter.api.Assertions.*;

import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.service.SimulationInitializer;
import com.example.chimpanzee_simulation.service.SimulationInitializerImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SimulationInitializerImplTest {

    @Test
    void 초기_상태_생성_시_기본_객체_수와_환경이_정상적으로_설정된다() {
        // given
        SimulationInitializer initializer = new SimulationInitializerImpl();

        // when
        SimulationState state = initializer.createInitialState();

        // then
        assertNotNull(state, "state는 null이어서는 안된다.");
        assertEquals(0, state.getTurnNumber(), "초기 턴 번호는 0이어야 한다.");

        assertNotNull(state.getChimpanzees(), "초기 침팬지 리스트는 null이어서는 안 된다.");
        assertFalse(state.getChimpanzees().isEmpty(), "초기 침팬지 리스트는 비어서는 안 된다.");

        assertNotNull(state.getEnvironment(), "초기 Environment는 null이면 안 된다.");
    }

    @Test
    void 초기_집단은_모두_alive_상태이며_알파는_존재하지_않는다() {
        // given
        SimulationInitializer initializer = new SimulationInitializerImpl();

        // when
        SimulationState state = initializer.createInitialState();
        List<Chimpanzee> chimps = state.getChimpanzees();

        // then
        assertTrue(
                chimps.stream().allMatch(Chimpanzee::isAlive),
                "초기에는 모든 침팬지가 alive 상태여야 한다."
        );

        assertTrue(
                chimps.stream().noneMatch(Chimpanzee::isAlpha),
                "초기에는 alpha 침팬지가 존재하지 않아야 한다."
        );

        assertTrue(
                chimps.stream().allMatch(c -> c.getDeathReason() == DeathReason.NONE),
                "초기 DeathReason은 모두 NONE이어야 한다."
        );
    }

    @Test
    void 초기_침팬지_능력치는_정의된_범위_내에서_생성된다() {
        // given
        SimulationInitializer initializer = new SimulationInitializerImpl();

        // when
        SimulationState state = initializer.createInitialState();
        List<Chimpanzee> chimps = state.getChimpanzees();

        // then
        for (Chimpanzee c : chimps) {
            assertTrue(c.getHealth() >= 0 && c.getHealth() <= 100, "health 범위 오류");
            assertTrue(c.getStrength() >= 0 && c.getStrength() <= 100, "strength 범위 오류");
            assertTrue(c.getAgility() >= 0 && c.getAgility() <= 100, "agility 범위 오류");

            assertTrue(c.getReproductionRate() >= 0.0 && c.getReproductionRate() <= 1.0,
                    "reproductionRate 범위 오류");

            assertTrue(c.getLongevity() >= 36 && c.getLongevity() <= 45,
                    "longevity 범위 오류");

            assertNotNull(c.getSex(), "sex는 null이면 안 된다.");
            assertNotNull(c.getAgeCategory(), "AgeCategory는 null이면 안 된다.");
        }
    }

    @Test
    void 초기_환경의_food는_개체_수_곱하기_5_규칙을_준수한다() {
        // given
        SimulationInitializer initializer = new SimulationInitializerImpl();

        // when
        SimulationState state = initializer.createInitialState();
        List<Chimpanzee> chimps = state.getChimpanzees();
        Environment env = state.getEnvironment();

        // expected
        int expectedFood = chimps.size() * 5;

        // then
        assertEquals(expectedFood, env.getFood(),
                "food는 (개체 수 * 5) 값이어야 한다.");
    }

    @Test
    void 초기_환경의_날씨와_위험도는_정상적인_값이어야_한다() {
        // given
        SimulationInitializer initializer = new SimulationInitializerImpl();

        // when
        SimulationState state = initializer.createInitialState();
        Environment env = state.getEnvironment();

        // then
        assertNotNull(env.getWeather(), "weather는 null이면 안 된다.");
        assertTrue(env.getDangerLevel() >= 0.0 && env.getDangerLevel() <= 1.0,
                "dangerLevel은 0.0~1.0 범위여야 한다.");

        boolean isValidWeather = false;
        for (Weather w : Weather.values()) {
            if (w == env.getWeather()) {
                isValidWeather = true;
                break;
            }
        }
        assertTrue(isValidWeather, "weather는 Weather enum 값이어야 한다.");
    }
}
