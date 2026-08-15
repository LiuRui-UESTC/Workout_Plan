package com.project.dev.code_sports_rui_and_fei.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

/**
 * 具体运动项目记录（如深蹲 50kg x 10次 x 3组）
 */
@Entity
@Table(name = "exercise_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    /** 运动名称，如：深蹲、卧推、引体向上 */
    @Column(nullable = false)
    private String exerciseName;

    /** 组数 */
    private Integer sets;

    /** 每组次数 */
    private Integer reps;

    /** 重量（kg） */
    private Double weight;

    /** 备注 */
    private String notes;
}
