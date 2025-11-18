package com.example.chimpanzee_simulation.domain.model;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;

import java.util.Random;

public class Chimpanzee {

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

    private Chimpanzee(Long id, int age, Sex sex, int health, int strength, int agility, double reproductionRate, int longevity, boolean alpha, boolean alive, DeathReason deathReason, AgeCategory ageCategory) {
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
    }

    // 초기 랜덤 객체 생성 전용
    public static Chimpanzee randomInitial(Long id, Random random) {
        int age = random.nextInt(41);
        AgeCategory ageCategory = resolveAgeCategory(age);

        Sex sex;
        if (random.nextBoolean()) {
            sex = Sex.MALE;
        } else {
            sex = Sex.FEMALE;
        }

        int health = 60 + random.nextInt(41);
        int strength = random.nextInt(101);
        int agility = random.nextInt(101);

        double reproductionRate = random.nextDouble();

        int longevity = 36 + random.nextInt(10);

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
                ageCategory
        );
    }

    private static AgeCategory resolveAgeCategory(int age) {
        if (age <= 4) return AgeCategory.INFANT;
        if (age <= 7) return AgeCategory.JUVENILE;
        if (age <= 12) return AgeCategory.ADOLESCENT;
        if (age <= 20) return AgeCategory.YOUNG_ADULT;
        if (age <= 35) return AgeCategory.ADULT;
        return AgeCategory.ELDER;
    }
}
