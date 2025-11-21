package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.enums.Weather;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.Environment;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlphaResolutionServiceImplTest {

    @Test
    void resolveAlpha_whenNoCurrentAlpha_shouldElectHighestScoreAdultMale() {
        // given
        // 환경은 알파 로직에 직접 영향 없으므로 최소값으로 생성
        Environment env = Environment.of(0, Weather.NORMAL, 0.0);

        // 후보 1: 성체 수컷, health 80, strength 50, agility 50
        Chimpanzee male1 = Chimpanzee.ofFull(
                1L,
                15,                         // age
                Sex.MALE,
                80,                         // health
                50,                         // strength
                50,                         // agility
                0.5,                        // reproductionRate
                40,                         // longevity
                false,                      // alpha
                true,                       // alive
                DeathReason.NONE,
                AgeCategory.YOUNG_ADULT,    // ageCategory
                0,                          // birthTurn
                false,                      // pregnant
                -1,
                null
        );

        // 후보 2: 성체 수컷, health 90, strength 80, agility 20
        // score = 0.7*80 + 0.3*20 = 56 + 6 = 62
        Chimpanzee male2 = Chimpanzee.ofFull(
                2L,
                25,
                Sex.MALE,
                90,
                80,
                20,
                0.5,
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

        // 후보 3: health 부족(50)이라 후보에서 제외되어야 함
        Chimpanzee male3LowHealth = Chimpanzee.ofFull(
                3L,
                20,
                Sex.MALE,
                50,                         // health < 60 → 제외
                90,
                90,
                0.5,
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

        // 후보 4: 성체 암컷 → 성체 수컷 조건에서 제외
        Chimpanzee femaleAdult = Chimpanzee.ofFull(
                4L,
                22,
                Sex.FEMALE,
                90,
                90,
                90,
                0.5,
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

        List<Chimpanzee> chimps = Arrays.asList(
                male1,
                male2,
                male3LowHealth,
                femaleAdult
        );

        // seed 고정 → AlphaResolutionServiceImpl 내부 Random(123L) 기반으로 결정적 동작
        long seed = 123L;

        SimulationState state = new SimulationState(
                0,          // turn
                chimps,
                env,
                seed
        );

        TurnLog log = new TurnLog(0);
        AlphaResolutionService service = new AlphaResolutionServiceImpl();

        // when
        service.resolveAlpha(state, log);

        // then
        // 1) 알파는 정확히 1마리여야 한다.
        long alphaCount = chimps.stream()
                .filter(Chimpanzee::alpha)
                .count();
        assertEquals(1L, alphaCount, "알파는 정확히 한 마리여야 한다.");

        // 2) 기대되는 알파는 male2 (id=2) 이다. (점수가 가장 높음)
        Chimpanzee elected = chimps.stream()
                .filter(Chimpanzee::alpha)
                .findFirst()
                .orElseThrow(() -> new AssertionError("알파가 선출되지 않았다."));

        assertEquals(2L, elected.getId(), "가장 높은 score를 가진 성체 수컷이 알파로 선출되어야 한다.");

        // 3) health 조건 및 성별/연령 조건이 제대로 적용되었는지 간접적으로 검증
        assertFalse(male3LowHealth.alpha(), "health < 60 인 수컷은 알파 후보에서 제외되어야 한다.");
        assertFalse(femaleAdult.alpha(), "암컷은 알파 후보가 될 수 없다.");
    }
}
