package com.project.dev.code_sports_rui_and_fei.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体测数据记录
 */
@Entity
@Table(name = "body_measurements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate recordDate;

    /** 体重（kg） */
    private Double weight;

    /** 体脂率（%） */
    private Double bodyFatPercentage;

    /** 胸围（cm） */
    private Double chestCircumference;

    /** 腰围（cm） */
    private Double waistCircumference;

    /** 臀围（cm） */
    private Double hipCircumference;

    /** 臂围（cm） */
    private Double armCircumference;

    /** 大腿围（cm） */
    private Double thighCircumference;

    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
