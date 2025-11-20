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
        int strength = generateInitialStrength(random);
        int agility = generateInitialAgility(random);

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

    // 초기 힘 능력치를 생성한다. 0~100
    private static int generateInitialStrength(Random random) {
        return random.nextInt(101); // 0~100
    }

    // 초기 민첩 능력치를 생성한다. 0~100
    private static int generateInitialAgility(Random random) {
        return random.nextInt(101); // 0~100
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
    public void conceive(int currentTurn) {
        this.pregnant = true;
        this.pregnancyDueTurn = currentTurn + GESTATION_PERIOD;
    }

    // 출산 처리(상태 리셋)
    public void giveBirth() {
        this.pregnant = false;
        this.pregnancyDueTurn = -1;
    }

    // ** 부모의 유전자를 받아 자손 생성 (추후 팩토리로)
    public static Chimpanzee createOffspring(Long newId, Chimpanzee father, Chimpanzee mother, int currentTurn, Random random) {

        Sex newSex = generateInitialSex(random);

        // 능력치 유전
        int newStrength = inheritStat(father.strength, mother.strength, random);
        int newAgility = inheritStat(father.agility, mother.agility, random);
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

    private void setDeathReason(DeathReason deathReason) {
        this.deathReason = deathReason;
    }

    private void setAlive(boolean alive) {
        this.alive = alive;
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
    // 테스트용 getter
    Long getId() {
        return id;
    }

    int getAge() {
        return age;
    }

    Sex getSex() {
        return sex;
    }

    int getHealth() {
        return health;
    }

    int getStrength() {
        return strength;
    }

    int getAgility() {
        return agility;
    }

    double getReproductionRate() {
        return reproductionRate;
    }

    int getLongevity() {
        return longevity;
    }

    boolean isAlpha() {
        return alpha;
    }

    boolean isAlive() {
        return alive;
    }

    DeathReason getDeathReason() {
        return deathReason;
    }

    AgeCategory getAgeCategory() {
        return ageCategory;
    }
}
