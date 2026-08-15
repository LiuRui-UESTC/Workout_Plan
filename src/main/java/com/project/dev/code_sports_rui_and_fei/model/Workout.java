package com.project.dev.code_sports_rui_and_fei.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 健身记录实体
 */
@Entity
@Table(name = "workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutType workoutType;

    @Column(nullable = false)
    private LocalDate workoutDate;

    /** 运动时长（分钟） */
    private Integer durationMinutes;

    /** 消耗卡路里 */
    private Double caloriesBurned;

    /** 运动距离（公里） */
    private Double distance;

    private String notes;

    /** 心情评分 1-5 */
    private Integer moodRating;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExerciseRecord> exercises = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
