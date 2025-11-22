# 🐒 Chimpanzee Simulation

침팬지 집단의 생존과 번식, 우두머리 경쟁을 간단한 규칙으로 시뮬레이션하는 Spring Boot 콘솔 애플리케이션입니다.  
먹이 생산/소비, 연령에 따른 능력치 성장, 알파(우두머리) 도전, 출산/번식, 노화와 자연사까지 한 턴 단위로 진행됩니다.

---

## 📌 핵심 개념

- **침팬지 개체 (`Chimpanzee`)**
  - 속성: 나이, 성별, 체력(health), 힘(strength), 민첩(agility), 번식률, 기대수명(longevity), 알파 여부, 임신 상태 등
  - 연령대(`AgeCategory`)에 따라 능력치 성장/하락 곡선을 따름
  - 4턴이 지날 때마다 나이가 1살 증가하고, 노화에 따라 자연사 가능

- **환경 (`Environment`)**
  - 속성: 먹이(food), 날씨(`Weather`), 위험도(dangerLevel)
  - 턴마다 날씨가 마르코프 체인 규칙으로 변화하고, 날씨에 따라 위험도가 서서히 조정됨
  - 날씨·위험도는 먹이 생산량과 생존 난이도에 영향을 미침

- **시뮬레이션 상태 (`SimulationState`)**
  - 현재 턴 번호, 침팬지 리스트, 환경, 공용 난수(`Random`)를 보관
  - 하나의 시드(`randomSeed`) 기반으로 전체 시뮬레이션 난수 흐름을 재현 가능

- **턴 로그 (`TurnLog`)**
  - 한 턴에 발생한 주요 이벤트(날씨/위험도 변화, 우두머리 요약, 먹이 생산/소비, 번식/출산, 사망 등)를 텍스트 메시지로 기록

- **시뮬레이션 결과 (`SimulationResult`)**
  - 집단 이름, 총 진행 턴 수, 최종 개체 수, 멸종 여부(extinction)를 요약

---

## ⚙️ 전체 처리 흐름

시작부터 종료까지의 고수준 흐름은 다음과 같습니다.

```mermaid
flowchart TD
    A[애플리케이션 시작<br/>ChimpanzeeSimulationApplication] --> B[SimulationRunner]
    B --> C[SimulationInitializer<br/>초기 상태 생성]
    C --> D[SimulationEngine<br/>run(...)]
    D --> E{현재 턴 < 최대 턴?}
    E -- 예 --> F[TurnProcessor<br/>한 턴 처리]
    F --> E
    E -- 아니오 --> G[SimulationResult 생성 및 출력]
```

### 한 턴 처리 파이프라인

```mermaid
flowchart TD
    TP[TurnProcessorImpl.runTurn] --> W[EnvironmentService<br/>날씨/위험도 업데이트]
    W --> A[AlphaSummaryService<br/>우두머리 요약 로그]
    A --> AG[AgingService<br/>나이 증가 + 자연사 판정]
    AG --> FP[FoodProductionService<br/>먹이 생산]
    FP --> AR[AlphaResolutionService<br/>알파 선출/도전/결투]
    AR --> FC[FoodConsumptionService<br/>먹이 소비/우선순위 배분]
    FC --> R[ReproductionService<br/>출산/짝짓기/신규 개체 추가]
    R --> N[턴 증가<br/>state.nextTurn()]
```

각 단계는 `TurnLog`에 요약 로그를 남기며, `SimulationEngineImpl`이 이를 콘솔에 출력합니다.

---

## 🧬 도메인 모델 개요

### Chimpanzee (침팬지)

- **기본 속성**
  - `age`: 나이 (4턴 = 1살)
  - `sex`: 성별 (`MALE`, `FEMALE`)
  - `health`: 체력 (0~100)
  - `strength`: 힘 (0~100)
  - `agility`: 민첩 (0~100)
  - `reproductionRate`: 번식률 (0.0~1.0)
  - `longevity`: 기대수명 (36~45세)
  - `ageCategory`: 연령대 (`INFANT`, `JUVENILE`, `ADOLESCENT`, `YOUNG_ADULT`, `ADULT`, `ELDER`)
  - `alpha`: 우두머리 여부
  - `alive`: 생존 여부
  - `deathReason`: 사망 원인 (`STARVATION`, `DISEASE`, `PREDATOR`, `OLD_AGE`, `ALPHA_FIGHT` 등)

- **초기 생성**
  - `Chimpanzee.randomInitial(...)`에서 나이·연령대에 따라 힘/민첩 초기 범위를 다르게 생성
    - INFANT: 0~30, JUVENILE: 20~60, ADOLESCENT: 40~80, YOUNG_ADULT: 50~90, ADULT: 60~95, ELDER: 40~80

- **출생(offspring)**
  - `createOffspring(...)`에서 새로 태어나는 아기 침팬지는 힘/민첩을 0~10 범위에서 시작
  - 기대수명(longevity)은 부모 평균 ±10% 범위에서 유전

- **연령 기반 성장 곡선**
  - 4턴마다 나이가 1살 증가할 때, 연령대에 따라 힘/민첩이 확률적으로 변함
  - 예시:
    - INFANT: 거의 항상 +3~+7, 드물게 더 적거나 더 많이 성장
    - ADOLESCENT: +1~+5 범위에서 성장, 평균적으로 10대 후반에 70~80 수준 도달
    - YOUNG_ADULT: 유지/소폭 성장 위주, 가끔 하락
    - ADULT: 서서히 하락이 기본, 매우 드물게 +2 상승으로 “괴물” 개체 유지
    - ELDER: 하락이 기본, 극히 드물게 유지/소폭 상승 → 전설적인 노장 가능

- **자연사**
  - 나이가 기대수명을 넘으면 턴마다 노쇠 사망 확률이 증가
  - 사망 시 `alive = false`, `deathReason = OLD_AGE` 등으로 상태 업데이트

---

## 🌦️ 환경과 날씨/위험도

### Environment

- `food`: 현재 먹이량
- `weather`: 날씨 (`SUNNY`, `NORMAL`, `RAINY`, `STORM`, `DROUGHT`)
- `dangerLevel`: 위험도 (0.0~1.0)

### 날씨 변화 규칙

- `Environment.updateWeather(Random)`에서 **마르코프 체인**으로 다음 날씨 결정
  - 예: `SUNNY`일 때는 SUNNY 유지 확률이 높고, STORM/DROUGHT로는 낮은 확률로 이동
  - `SimulationState.random()`을 사용해 매 턴 자연스러운 난수 시퀀스 유지

### 위험도 조정

- `Environment.adjustDangerLevelForWeather(weather)`에서 날씨별 target 값에 수렴하도록 dangerLevel 보정
  - SUNNY: 0.1, NORMAL: 0.2, RAINY: 0.25, STORM: 0.6, DROUGHT: 0.7
  - 매 턴 `dangerLevel = 0.8 * 현재 + 0.2 * target` 형태로 서서히 변화
  - 초기 dangerLevel은 0.3~0.7 범위에서 시작

환경 관련 요약 로그 예시:
- `날씨 변화: SUNNY → STORM, 위험도: 0.35 → 0.52`
- `날씨 변화 없음 (현재: NORMAL), 위험도: 0.40 → 0.38`

---

## 🍌 먹이 생산과 소비

### 먹이 생산 (`FoodProductionServiceImpl`)

- 생존 중인 침팬지 수, 날씨(`Weather.productionMultiplier()`), 위험도에 따라 생산량 계산
- 날씨가 좋고 위험도가 낮을수록 더 많은 먹이가 생산됨
- 예시 요약 로그:
  - `먹이 생산: 생산량=120, 생존 개체 수=18, 날씨=SUNNY, 위험도=0.32`

### 먹이 소비 및 우선순위 배분 (`FoodConsumptionServiceImpl`)

- 모든 생존 침팬지를 **우선순위**에 따라 정렬 후, 남은 food에서 순서대로 배분:
  1. 알파 성체 수컷
  2. 일반 성체 수컷
  3. 성체 암컷
  4. 청소년/유년기
  5. 유아 + 노령
- 기본 필요량:
  - 일반: 1턴 기준 5
  - 알파: 5 * 1.2 ≒ 6
- 배분 결과에 따라 체력이 증가하거나, 부족하면 감소 및 굶주림 사망 가능
- 예시 요약 로그:
  - `먹이 분배 규칙: 침팬지는 기본 5의 먹이가 필요하며, 우두머리는 약 6의 먹이가 필요합니다.`
  - `정상적으로 먹이를 섭취한 침팬지 ID: 1, 2, 3, 4, 7`
  - `충분히 먹지 못한 침팬지 ID: 8, 9`
  - `침팬지 ID 9는 먹이를 전혀 먹지 못했습니다. (건강 -10 감소)`
  - `침팬지 ID 12가 굶주려 사망했습니다.`

---

## 👑 우두머리(알파) 규칙

### 알파 선출 (`AlphaResolutionServiceImpl`)

- 알파가 없는 경우, 조건을 만족하는 수컷 중에서 새로운 우두머리 선출
  - 조건:
    - 살아 있음
    - 성별: 수컷
    - 연령대: `YOUNG_ADULT` 또는 `ADULT`
    - 체력 ≥ 60
  - 후보들 중 힘·민첩을 가중합한 score로 1등 그룹을 뽑고, 그 중 랜덤 선택

### 알파 도전 & 결투

1. **약화 판정**  
   - 알파가 약해졌다고 판단되면, 도전 가능 상태가 됨
   - 기준:
     - `health < 70` 또는
     - `age > longevity * 0.7`

2. **도전 발생 확률**
   - 알파가 약한 상태에서 일정 확률(`CHALLENGE_PROBABILITY ≒ 30%`)로 도전 이벤트 발생

3. **도전자 후보**
   - 살아 있는 성체 수컷 중 알파를 제외하고,  
     `strength > alphaStrength * 0.7` 조건을 만족하는 개체들

4. **결투 및 피해**
   - fightScore = strength * 0.6 + agility * 0.4 + random(0~10)
   - 점수 차이에 따라 패자 피해량(20~80)을 결정하고 체력 감소
   - 승자도 `max(5, 패자피해/4)` 정도의 피로 피해를 받아, 여러 번 싸우면 점점 약해짐
   - 패자의 체력이 0 이하가 되면 `ALPHA_FIGHT` 사망 처리

5. **알파 교체**
   - 승자가 기존 알파가 아니면, 알파 플래그를 넘겨 우두머리 교체

요약 로그 예시:
- `[우두머리] 우두머리를 선출했습니다(기존 우두머리 없음): 침팬지 ID=3, 점수=78.5`
- `[우두머리] 우두머리가 약하지만 이번 턴에는 도전이 발생하지 않았습니다.`
- `[우두머리] 도전 발생: 우두머리=3 vs 도전자=7`
- `[우두머리] 결투 결과: 승자=7, 패자=3, 우두머리 점수=82.3, 도전자 점수=85.1, 점수차=2.8, 패자 피해=34, 승자 피해=12`
- `[우두머리] 우두머리 교체: 새 우두머리=7 (이전 우두머리=3)`

---

## ❤️ 번식과 출산

### 번식 가능 조건

- `canMate()`:
  - 살아 있음
  - 임신 상태가 아님(암컷)
  - 체력 ≥ 50
  - 연령대: `YOUNG_ADULT` 또는 `ADULT`

### 짝짓기 & 임신

- 번식 가능한 수컷/암컷을 모아 무작위로 페어링
- 수컷·암컷의 번식률 평균을 기반으로 임신 성공 확률 결정
  - 성공 시, 암컷은 `GESTATION_PERIOD`(기본 3턴) 후 출산 예정

### 출산

- 임신 만료 턴에 도달하면 출산 진행
  - 아기 침팬지 한 마리 생성(힘/민첩 0~10, 기대수명은 부모 기반 유전)
  - 어미는 임신 상태 해제, 아버지/자식 정보는 로그로 남김

---

## 🧪 실행 방법

프로젝트 루트에서 Gradle을 통해 실행할 수 있습니다.

- 테스트 실행

```bash
./gradlew test
```

- 시뮬레이션 실행 (콘솔 로그 출력)

```bash
./gradlew bootRun
```

기본 설정:
- 초기 개체 수: 20 (`SimulationInitializerImpl`)
- 최대 턴 수: 50 (`SimulationRunner` 내 `SimulationConfig`)
- 집단 이름: `"Default Colony"`

실행 후 콘솔에는 각 턴의 요약 로그와 최종 결과가 출력됩니다.

---

## 🧱 설계 포인트 & 확장 아이디어

- **재현 가능한 난수**
  - `SimulationState.randomSeed`와 내부 `Random`을 통해, 같은 시드에 대해 항상 같은 시뮬레이션 결과를 얻을 수 있습니다.
  - 디버깅/튜닝 시 특정 시드를 고정해 시나리오를 재현할 수 있습니다.

- **도메인 중심 구조**
  - `domain/model`에 순수 도메인 객체(상태)를 두고,
  - `service` 계층에서 턴 처리/규칙을 구현하는 구조라, 규칙 변경·추가가 비교적 쉽습니다.

- **확장 아이디어**
  - 로그 레벨 개념 도입 (요약 vs 상세) 및 파일 출력
  - 질병, 포식자, 환경 이벤트(폭풍/가뭄 이벤트 카드) 추가
  - 집단 간 경쟁(여러 콜로니를 동시에 시뮬레이션) 또는 시각화 UI 연동

---

## 📂 주요 패키지 구조

- `com.example.chimpanzee_simulation`
  - `ChimpanzeeSimulationApplication` – Spring Boot 엔트리 포인트
  - `domain.enums` – `AgeCategory`, `Sex`, `Weather`, `DeathReason` 등
  - `domain.model` – `Chimpanzee`, `Environment`, `SimulationState`, `SimulationResult`, `TurnLog` 등
  - `service`
    - `SimulationInitializer` / `SimulationInitializerImpl`
    - `SimulationEngine` / `SimulationEngineImpl`
    - `TurnProcessor` / `TurnProcessorImpl`
    - `EnvironmentService` / `EnvironmentServiceImpl`
    - `AlphaSummaryService` / `AlphaSummaryServiceImpl`
    - `AgingService` / `AgingServiceImpl`
    - `FoodProductionService` / `FoodProductionServiceImpl`
    - `FoodConsumptionService` / `FoodConsumptionServiceImpl`
    - `ReproductionService` / `ReproductionServiceImpl`

이 README는 프로젝트 전반의 구조와 흐름을 한눈에 이해할 수 있도록 요약한 것이며,  
각 규칙의 상세 구현은 `src/main/java/com/example/chimpanzee_simulation` 하위 코드를 참고하면 됩니다.
