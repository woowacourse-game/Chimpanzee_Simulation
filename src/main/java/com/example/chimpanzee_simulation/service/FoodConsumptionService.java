package com.example.chimpanzee_simulation.service;

import com.example.chimpanzee_simulation.domain.model.SimulationState;
import com.example.chimpanzee_simulation.domain.model.TurnLog;

/**
 * R-FOOD-01: 먹이 소비량 & 배분
 *
 * - environment.food를 우선순위에 따라 개체들에게 분배하고
 * - 각 개체의 health를 섭취량에 따라 조정하며
 * - 분배 후 남은 food를 environment에 반영한다.
 *
 * 죽음 판정은 이 단계에서는 하지 않고,
 * health 변화만 반영한 뒤 후속 단계에서 health <= 0 인 개체를 일괄 사망 처리한다.
 */
public interface FoodConsumptionService {

    void consumeAndDistribute(SimulationState state, TurnLog log);
}
