package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;

/**
 * R-ALPHA-01: 알파 선출 & 도전 규칙
 *
 * - 알파가 없을 때: 성체 수컷 후보 중에서 새로운 알파 선출
 * - 알파가 약해졌을 때: 일정 확률로 도전 이벤트 발생, 결투 결과에 따라 알파 교체
 * - 패자는 피해를 입고, 경우에 따라 사망(DeathReason.ALPHA_FIGHT) 처리
 *
 * Random 사용 방식(시드, 재현성 보장)은 구현체에서 SimulationState에 맞추어 처리한다.
 */
public interface AlphaResolutionService {

    /**
     * 현재 시뮬레이션 상태를 기반으로 알파 선출/도전/교체 로직을 수행하고,
     * 그 결과를 TurnLog에 기록한다.
     */
    void resolveAlpha(SimulationState state, TurnLog log);
}
