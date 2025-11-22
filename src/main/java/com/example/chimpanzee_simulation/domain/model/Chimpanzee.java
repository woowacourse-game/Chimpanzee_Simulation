package com.example.chimpanzee_simulation.domain.model;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;

import java.util.Random;

public class Chimpanzee {
    private static final int GESTATION_PERIOD = 3;

    private Long id;
    private int age;                    // 턴 4개 -> 1년 (+1살)
    private Sex sex;                    // enum Sex { MALE, FEMALE}

    private int health;                 // 0 ~ 100 (체력)
    private int strength;               // 0 ~ 100 (힘)
    private int agility;                // 0 ~ 100 (민첩)

    private double reproductionRate;    // 0.0 ~ 1.0 (번식률)
    private int longevity;              // 기대수명 랜덤 36세 ~ 45세

    private boolean alpha;              // 우두머리
    private boolean alive;              // 살아있는지
    private DeathReason deathReason;    // enum { STARVATION, DISEASE, PREDATOR, OLD_AGE, OTHER, NONE }
    private AgeCategory ageCategory;    // enum { INFANT, JUVENILE, ADOLESCENT, YOUNG_ADULT, ADULT, ELDER}
    private int birthTurn;

    private boolean pregnant;
    private int pregnancyDueTurn;
    private Long pregnancyFatherId;     // 임신 중인 상태에서 아버지 ID 저장

    private Chimpanzee(Long id, int age, Sex sex, int health, int strength, int agility, double reproductionRate, int longevity, boolean alpha, boolean alive, DeathReason deathReason, AgeCategory ageCategory, int birthTurn, boolean pregnant, int pregnancyDueTurn) {
        this.id = id;
        this.age = age;
        this.sex = sex;
        this.health = health;
        this.strength = strength;
        this.agility = agility;
        this.reproductionRate = reproductionRate;
        this.longevity = longevity;
        this.alpha = alpha;
        this.alive = alive;
        this.deathReason = deathReason;
        this.ageCategory = ageCategory;
        this.birthTurn = birthTurn;
        this.pregnant = pregnant;
        this.pregnancyDueTurn = pregnancyDueTurn;
    }

    public boolean alive() {
        return alive;
    }

    // 초기 랜덤 객체 생성 전용
    public static Chimpanzee randomInitial(Long id, Random random, int currentTurn) {
        int age = generateInitialAge(random);
        AgeCategory ageCategory = resolveAgeCategory(age);

        Sex sex = generateInitialSex(random);

        int health = generateInitialHealth(random);
        int strength = generateInitialStrength(ageCategory, random);
        int agility = generateInitialAgility(ageCategory, random);

        double reproductionRate = generateInitialReproductionRate(random);
        int longevity = generateInitialLongevity(random);

        return new Chimpanzee(
                id,
                age,
                sex,
                health,
                strength,
                agility,
                reproductionRate,
                longevity,
                false,
                true,
                DeathReason.NONE,
                ageCategory,
                currentTurn,
                false,
                -1
        );
    }

    // 초기 나이를 생성한다 0 ~ 40살
    private static int generateInitialAge(Random random) {
        return random.nextInt(41); // 0~40
    }

    // 초기 성별을 무작위로 선택한다
    private static Sex generateInitialSex(Random random) {
        boolean male = random.nextBoolean();
        if (male) {
            return Sex.MALE;
        } else {
            return Sex.FEMALE;
        }
    }

    // 초기 체력을 생성한다 60~100
    private static int generateInitialHealth(Random random) {
        return 60 + random.nextInt(41);
    }

    // 연령대 기반 초기 힘 능력치를 생성한다.
    private static int generateInitialStrength(AgeCategory ageCategory, Random random) {
        switch (ageCategory) {
            case INFANT:
                // 1~25
                return randomBetween(random, 1, 25);
            case JUVENILE:
                // 20~60
                return randomBetween(random, 20, 60);
            case ADOLESCENT:
                // 40~80
                return randomBetween(random, 40, 80);
            case YOUNG_ADULT:
                // 50~90
                return randomBetween(random, 50, 90);
            case ADULT:
                // 60~95
                return randomBetween(random, 60, 95);
            case ELDER:
                // 40~80
                return randomBetween(random, 40, 80);
            default:
                return random.nextInt(101);
        }
    }

    // 연령대 기반 초기 민첩 능력치를 생성한다. (현재는 힘과 동일 범위)
    private static int generateInitialAgility(AgeCategory ageCategory, Random random) {
        // 힘과 동일한 분포를 사용하지만, 필요 시 다르게 조정 가능
        return generateInitialStrength(ageCategory, random);
    }

    private static double generateInitialReproductionRate(Random random) {
        return random.nextDouble(); // 0.0 ~ 1.0
    }

    private static int generateInitialLongevity(Random random) {
        return 36 + random.nextInt(10); // 36~45
    }

    // 나이를 기반으로 연령대 결정
    private static AgeCategory resolveAgeCategory(int age) {
        if (age <= 4) return AgeCategory.INFANT;
        if (age <= 7) return AgeCategory.JUVENILE;
        if (age <= 12) return AgeCategory.ADOLESCENT;
        if (age <= 20) return AgeCategory.YOUNG_ADULT;
        if (age <= 35) return AgeCategory.ADULT;
        return AgeCategory.ELDER;
    }

    /**
     * 연령대별 능력치 성장/하락 곡선에서
     * 한 살 증가 시 strength/agility에 적용할 변화량(Δ)를 샘플링한다.
     *
     * 이 메서드는 아직 어디에서도 호출되지 않으며,
     * 이후 나이 증가 로직에 연결하여 실제 stat 성장/하락을 적용하는 데 사용된다.
     */
    private static int sampleGrowthDelta(AgeCategory ageCategory, Random random) {
        double r = random.nextDouble();

        switch (ageCategory) {
            case INFANT:
                // INFANT (0–4세)
                // 20%: +1~+2, 60%: +3~+5, 20%: +6~+7
                if (r < 0.2) {
                    return randomBetween(random, 1, 2);
                } else if (r < 0.8) {
                    return randomBetween(random, 3, 5);
                } else {
                    return randomBetween(random, 6, 7);
                }
            case JUVENILE:
                // JUVENILE (5–7세)
                // 20%: +1~+2, 60%: +3~+4, 20%: +5~+6
                if (r < 0.2) {
                    return randomBetween(random, 1, 2);
                } else if (r < 0.8) {
                    return randomBetween(random, 3, 4);
                } else {
                    return randomBetween(random, 5, 6);
                }
            case ADOLESCENT:
                // ADOLESCENT (8–12세)
                // 30%: +1~+2, 50%: +3~+4, 20%: +5
                if (r < 0.3) {
                    return randomBetween(random, 1, 2);
                } else if (r < 0.8) {
                    return randomBetween(random, 3, 4);
                } else {
                    return 5;
                }
            case YOUNG_ADULT:
                // YOUNG_ADULT (13–20세)
                // 10%: +2~+3, 40%: 0~+1, 30%: -1~0, 20%: -2~-3
                if (r < 0.1) {
                    return randomBetween(random, 2, 3);
                } else if (r < 0.5) {
                    return randomBetween(random, 0, 1);
                } else if (r < 0.8) {
                    return randomBetween(random, -1, 0);
                } else {
                    return randomBetween(random, -3, -2);
                }
            case ADULT:
                // ADULT (21–35세)
                // 5%: +2, 25%: 0~+1, 40%: -1~-2, 30%: -3~-4
                if (r < 0.05) {
                    return 2;
                } else if (r < 0.30) {
                    return randomBetween(random, 0, 1);
                } else if (r < 0.70) {
                    return randomBetween(random, -2, -1);
                } else {
                    return randomBetween(random, -4, -3);
                }
            case ELDER:
                // ELDER (36세 이상)
                // 5%: +1, 20%: 0, 35%: -1~-2, 40%: -3~-5
                if (r < 0.05) {
                    return 1;
                } else if (r < 0.25) {
                    return 0;
                } else if (r < 0.60) {
                    return randomBetween(random, -2, -1);
                } else {
                    return randomBetween(random, -5, -3);
                }
            default:
                // 방어적 기본값: 변화 없음
                return 0;
        }
    }

    private static int randomBetween(Random random, int minInclusive, int maxInclusive) {
        if (minInclusive == maxInclusive) {
            return minInclusive;
        }
        if (minInclusive > maxInclusive) {
            int tmp = minInclusive;
            minInclusive = maxInclusive;
            maxInclusive = tmp;
        }
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private static int clampStat(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    // 나이 증가 체크
    public boolean incrementAgeIfNeeded(int currentTurn) {
        int diff = currentTurn - this.birthTurn;

        if (diff <= 0) return false;

        if (diff % 4 == 0) {
            this.age += 1;
            this.ageCategory = resolveAgeCategory(this.age);
            return true;
        }
        return false;
    }

    /**
     * 현재 연령대에 따른 성장 곡선을 기반으로
     * strength / agility를 한 살 증가분만큼 갱신한다.
     */
    public void applyAgeBasedGrowth(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }

        int delta = sampleGrowthDelta(this.ageCategory, random);
        this.strength = clampStat(this.strength + delta);
        this.agility = clampStat(this.agility + delta);
    }

    // 자연사 판정
    public boolean checkNaturalDeath(int currentTurn, Random random) {
        if (this.age < this.longevity) {
            return false; // 자연사 없음
        }

        double base = 0.1 * (this.age - this.longevity + 1);
        if (base > 0.9) base = 0.9;

        double r = random.nextDouble();

        if (r < base) {
            this.alive = false;
            this.deathReason = DeathReason.OLD_AGE;
            return true;
        }
        return false;
    }

    // 사망과 사망이유 적용 메서드
    public void applyAliveAndDeathReason(DeathReason deathReason) {
        this.setDeathReason(deathReason);
        this.setAlive(false);
        this.health = 0;
        this.alpha = false;
    }

    public void applyHealthChange(int delta) {
        if (!alive) {
            return;
        }
        this.health += delta;
        if (this.health > 100) {
            this.health = 100;
        }
        if (this.health < 0) {
            this.health = 0;
        }
    }

    // 임신 가능 상태인지 검사
    public boolean canMate() {
        if (!alive) return false;
        if (pregnant) return false;
        if (health <50) return false;

        return ageCategory == AgeCategory.YOUNG_ADULT || ageCategory == AgeCategory.ADULT;
    }

    // 임신 시작 처리
    public void conceive(int currentTurn, Long fatherId) {
        this.pregnant = true;
        this.pregnancyDueTurn = currentTurn + GESTATION_PERIOD;
        this.pregnancyFatherId = fatherId;
    }

    // 출산 처리(상태 리셋)
    public void giveBirth() {
        this.pregnant = false;
        this.pregnancyDueTurn = -1;
        this.pregnancyFatherId = null;
    }

    // ** 부모의 유전자를 받아 자손 생성 (추후 팩토리로)
    public static Chimpanzee createOffspring(Long newId, Chimpanzee father, Chimpanzee mother, int currentTurn, Random random) {

        Sex newSex = generateInitialSex(random);

        // 힘/민첩은 아기 구간에서 시작하도록 낮은 범위에서 랜덤 부여
        int newStrength = randomBetween(random, 1, 10);
        int newAgility = randomBetween(random, 1, 10);
        int newLongevity = inheritStat(father.longevity, mother.longevity, random);

        // 새로운 침팬지의 체력은 100
        int newHealth = 100;
        double newReproductionRate = random.nextDouble();

        // 아기 침팬지 생성
        return new Chimpanzee(
                newId,
                0,      // age
                newSex,
                newHealth,
                newStrength,
                newAgility,
                newReproductionRate,
                newLongevity,
                false,      // alpha
                true,       // alive
                DeathReason.NONE,
                AgeCategory.INFANT,
                currentTurn,
                false,      // pregnant
                -1      // dueTurn
        );
    }

    // 부모 평균의 ±10% 범위 내에서 랜덤 값 결정
    private static int inheritStat(int val1, int val2, Random random) {
        double avg = (val1 + val2) / 2.0;
        double variance = avg * 0.1;    // 10%

        double min = avg - variance;
        double max = avg + variance;

        // min ~ max 사이 랜덤
        double result = min + (max - min) * random.nextDouble();

        // 0 ~ 100 벗어나는 값 방지
        return Math.max(0, Math.min(100, (int) Math.round(result)));
    }

    public static Chimpanzee ofFull(
            Long id,
            int age,
            Sex sex,
            int health,
            int strength,
            int agility,
            double reproductionRate,
            int longevity,
            boolean alpha,
            boolean alive,
            DeathReason deathReason,
            AgeCategory ageCategory,
            int birthTurn,
            boolean pregnant,
            int pregnancyDueTurn,
            Long pregnancyFatherId
    ) {
        Chimpanzee chimp = new Chimpanzee(
                id,
                age,
                sex,
                health,
                strength,
                agility,
                reproductionRate,
                longevity,
                alpha,
                alive,
                deathReason,
                ageCategory,
                birthTurn,
                pregnant,
                pregnancyDueTurn
        );
        chimp.pregnancyFatherId = pregnancyFatherId;
        return chimp;
    }

    private void setDeathReason(DeathReason deathReason) {
        this.deathReason = deathReason;
    }

    private void setAlive(boolean alive) {
        this.alive = alive;
    }

    // 이 개체를 알파로 지정
    public void assignAlpha() {
        this.alpha = true;
    }

    // 이 개체를 알파에서 해제
    public void revokeAlpha() {
        this.alpha = false;
    }

    public Sex sex(){
        return sex;
    }

    public AgeCategory ageCategory(){
        return ageCategory;
    }

    public boolean alpha(){
        return alpha;
    }
    public int health(){
        return health;
    }

    public Long getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }

    public int getHealth() {
        return health;
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }

    public double getReproductionRate() {
        return reproductionRate;
    }

    public int getLongevity() {
        return longevity;
    }

    public boolean isAlpha() {
        return alpha;
    }

    public boolean isAlive() {
        return alive;
    }

    public DeathReason getDeathReason() {
        return deathReason;
    }

    public AgeCategory getAgeCategory() {
        return ageCategory;
    }

    public boolean isPregnant() {
        return pregnant;
    }

    public int getPregnancyDueTurn() {
        return pregnancyDueTurn;
    }

    public Long getPregnancyFatherId() {
        return pregnancyFatherId;
    }
}
