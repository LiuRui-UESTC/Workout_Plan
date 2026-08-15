package com.project.dev.code_sports_rui_and_fei.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * 情侣共同目标
 */
@Entity
@Table(name = "couple_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    /** 目标类型: WORKOUT_COUNT(运动次数), WEIGHT_LOSS(减重), DISTANCE(总里程) */
    @Column(nullable = false)
    private String goalType;

    /** 目标值 */
    private Double targetValue;

    /** 当前进度 */
    @Builder.Default
    private Double currentValue = 0.0;

    @Builder.Default
    private Boolean completed = false;
}
