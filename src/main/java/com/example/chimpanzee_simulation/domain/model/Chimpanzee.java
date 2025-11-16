package com.example.chimpanzee_simulation.domain.model;

import com.example.chimpanzee_simulation.domain.enums.AgeCategory;
import com.example.chimpanzee_simulation.domain.enums.DeathReason;
import com.example.chimpanzee_simulation.domain.enums.Sex;

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
}
