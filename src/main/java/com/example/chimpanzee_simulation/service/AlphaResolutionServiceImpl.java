package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;
import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AlphaResolutionServiceImpl implements AlphaResolutionService {

    // 알파 도전 이벤트 발생 확률 (20%)
    private static final double CHALLENGE_PROBABILITY = 0.2;

    @Override
    public void resolveAlpha(SimulationState state, TurnLog log) {

        Chimpanzee currentAlpha = findCurrentAlpha(state);

        if (currentAlpha == null) {
            electAlpha(state, log);
            return;
        }

        // 알파가 이미 존재하는 경우: 알파 약화/노화 상태에서 도전/결투 처리
        handleAlphaChallenge(state, log, currentAlpha);
    }

    // ==========================
    // 1. 현재 알파 검색
    // ==========================

    private Chimpanzee findCurrentAlpha(SimulationState state) {
        return state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(Chimpanzee::alpha) // 실제 메서드명은 나중에 Chimpanzee 최신본에 맞게 조정
                .findFirst()
                .orElse(null);
    }

    // ==========================
    // 2. 알파 부재 시 선출
    // ==========================

    /**
     * 알파가 없을 때 새로운 알파를 선출한다.
     * 후보 조건:
     * - alive == true
     * - sex == MALE
     * - ageCategory == YOUNG_ADULT or ADULT
     * - health >= 60
     */
    private void electAlpha(SimulationState state, TurnLog log) {

        Random random = createRandomFromState(state);

        List<Chimpanzee> candidates = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(c -> c.sex() == Sex.MALE)
                .filter(c -> c.ageCategory() == AgeCategory.YOUNG_ADULT || c.ageCategory() == AgeCategory.ADULT)
                .filter(c -> c.health() >= 60)
                .toList();

        if (candidates.isEmpty()) {
            log.add("[우두머리] 조건을 만족하는 수컷이 없어 우두머리를 선출하지 못했습니다.");
            return;
        }

        List<ChimpanzeeScore> scored = candidates.stream()
                .map(c -> new ChimpanzeeScore(c, c.getStrength() * 0.7 + c.getAgility() * 0.3))
                .toList();

        double maxScore = scored.stream()
                .mapToDouble(s -> s.score)
                .max()
                .orElse(0.0);

        List<ChimpanzeeScore> top = scored.stream()
                .filter(s -> s.score == maxScore)
                .toList();

        ChimpanzeeScore winner = top.get(random.nextInt(top.size()));

        // 기존 알파 해제는 findCurrentAlpha에서 null인 경우만 오므로 없음
        winner.chimp.assignAlpha();

        log.add(String.format(
                "[우두머리] 우두머리를 선출했습니다(기존 우두머리 없음): 침팬지 ID=%s, 점수=%.2f",
                chimpLogKey(winner.chimp),
                winner.score
        ));
    }

    // ==========================
    // 3. 알파 도전 & 결투 처리
    // ==========================

    private void handleAlphaChallenge(SimulationState state, TurnLog log, Chimpanzee alpha) {
        Random random = createRandomFromState(state);

        // 3-1. 알파 약화/노화 상태인지 판단
        if (!isAlphaWeak(alpha)) {
            return; // 충분히 강하면 도전 없음
        }

        // 3-2. 확률적으로 도전 이벤트 발생 여부 결정
        double r = random.nextDouble();
        if (r >= CHALLENGE_PROBABILITY) {
            log.add("[우두머리] 우두머리가 약하지만 이번 턴에는 도전이 발생하지 않았습니다.");
            return;
        }

        // 3-3. 도전자 후보 선정
        List<Chimpanzee> challengerCandidates = state.chimpanzees().stream()
                .filter(Chimpanzee::alive)
                .filter(c -> c.sex() == Sex.MALE)
                .filter(c -> c.ageCategory() == AgeCategory.YOUNG_ADULT || c.ageCategory() == AgeCategory.ADULT)
                .filter(c -> c != alpha)
                .filter(c -> c.getStrength() > alpha.getStrength() * 0.8)
                .toList();

        if (challengerCandidates.isEmpty()) {
            log.add("[우두머리] 우두머리가 약하지만 도전할 만큼 강한 개체가 없습니다.");
            return;
        }

        Chimpanzee challenger = challengerCandidates.get(random.nextInt(challengerCandidates.size()));

        log.add(String.format(
                "[우두머리] 도전 발생: 우두머리=%s vs 도전자=%s",
                chimpLogKey(alpha),
                chimpLogKey(challenger)
        ));

        // 3-4. 결투 및 피해 처리
        resolveCombat(log, alpha, challenger, random);
    }

    /**
     * 알파 약화/노화 조건:
     * - health < 50
     * - 또는 age > longevity * 0.8
     */
    private boolean isAlphaWeak(Chimpanzee alpha) {
        if (alpha.health() < 50) {
            return true;
        }
        return alpha.getAge() > alpha.getLongevity() * 0.8;
    }

    /**
     * 결투 규칙:
     * fightScore = strength * 0.6 + agility * 0.4 + random(0~10)
     * 점수 차이(diff)에 비례하여 패자 피해량을 20 ~ 80로 스케일링
     */
    private void resolveCombat(TurnLog log,
                               Chimpanzee alpha,
                               Chimpanzee challenger,
                               Random random) {

        double alphaScore = fightScore(alpha, random);
        double challengerScore = fightScore(challenger, random);

        Chimpanzee winner;
        Chimpanzee loser;
        double diff = Math.abs(alphaScore - challengerScore);

        if (alphaScore > challengerScore) {
            winner = alpha;
            loser = challenger;
        } else if (challengerScore > alphaScore) {
            winner = challenger;
            loser = alpha;
        } else {
            // 동점일 경우 랜덤 승자
            if (random.nextBoolean()) {
                winner = alpha;
                loser = challenger;
            } else {
                winner = challenger;
                loser = alpha;
            }
            diff = 0.0; // 동점 시 diff=0, 추가 피해 거의 없음
        }

        // 패자 피해 계산
        int damage = calculateDamage(diff, random);
        loser.applyHealthChange(-damage);

        log.add(String.format(
                "[우두머리] 결투 결과: 승자=%s, 패자=%s, 우두머리 점수=%.2f, 도전자 점수=%.2f, 점수차=%.2f, 피해=%d",
                chimpLogKey(winner),
                chimpLogKey(loser),
                alphaScore,
                challengerScore,
                diff,
                damage
        ));

        // 사망 여부 처리
        if (loser.alive() && loser.health() <= 0) {
            loser.applyAliveAndDeathReason(DeathReason.ALPHA_FIGHT);
            log.add(String.format(
                    "[사망] 우두머리 결투로 사망: 침팬지 ID=%s, 이유=ALPHA_FIGHT",
                    chimpLogKey(loser)
            ));
        } else if (!loser.alive()) {
            // 이미 다른 규칙에서 죽게 되어 있었을 수 있는 경우 방어적으로 로그
            log.add(String.format(
                    "[사망] 결투 판정 이전에 이미 사망한 상태였습니다: 침팬지 ID=%s",
                    chimpLogKey(loser)
            ));
        }

        // 승/패에 따른 알파 플래그 갱신
        if (winner != alpha) {
            // challenger가 승리하여 알파 교체
            alpha.revokeAlpha();
            winner.assignAlpha();
            log.add(String.format(
                    "[우두머리] 우두머리 교체: 새 우두머리=%s (이전 우두머리=%s)",
                    chimpLogKey(winner),
                    chimpLogKey(alpha)
            ));
        } else {
            // 기존 알파 승리 → 알파 유지
            log.add(String.format(
                    "[우두머리] 우두머리 유지: %s",
                    chimpLogKey(alpha)
            ));
        }
    }

    private double fightScore(Chimpanzee chimp, Random random) {
        double ability = chimp.getStrength() * 0.6 + chimp.getAgility() * 0.4;
        double randomness = random.nextInt(11); // 0~10
        return ability + randomness;
    }

    /**
     * 피해량 계산:
     * baseDamage = 20
     * severityFactor = min(1.0, diff / 20.0)   // diff: fightScore 차이
     * extraDamage ∈ [0, 60] * severityFactor
     * totalDamage = 20 ~ 80 범위
     */
    private int calculateDamage(double diff, Random random) {
        double severityFactor = Math.min(1.0, diff / 20.0); // 0~1
        int extraBase = random.nextInt(61); // 0~60
        int extraDamage = (int) Math.round(extraBase * severityFactor);
        return 20 + extraDamage;
    }

    private Random createRandomFromState(SimulationState state) {
        return new Random(state.randomSeed());
    }

    private String chimpLogKey(Chimpanzee chimp) {
        return String.valueOf(chimp.getId());
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
