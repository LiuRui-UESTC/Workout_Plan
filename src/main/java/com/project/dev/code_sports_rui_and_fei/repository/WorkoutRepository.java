package com.project.dev.code_sports_rui_and_fei.repository;

import com.project.dev.code_sports_rui_and_fei.model.Workout;
import com.project.dev.code_sports_rui_and_fei.model.WorkoutType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByUserIdOrderByWorkoutDateDesc(Long userId);

    List<Workout> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
            Long userId, LocalDate start, LocalDate end);

    List<Workout> findByWorkoutDateBetweenOrderByWorkoutDateDesc(
            LocalDate start, LocalDate end);

    List<Workout> findByUserIdAndWorkoutType(Long userId, WorkoutType type);

    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user.id = :userId AND w.workoutDate BETWEEN :start AND :end")
    long countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM Workout w WHERE w.user.id = :userId AND w.workoutDate BETWEEN :start AND :end")
    Double sumCaloriesByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(w.durationMinutes), 0) FROM Workout w WHERE w.user.id = :userId AND w.workoutDate BETWEEN :start AND :end")
    Integer sumDurationByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(w.distance), 0) FROM Workout w WHERE w.user.id = :userId AND w.workoutDate BETWEEN :start AND :end")
    Double sumDistanceByUserIdAndDateRange(@Param("userId") Long userId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT w.workoutType, COUNT(w) FROM Workout w WHERE w.user.id = :userId GROUP BY w.workoutType")
    List<Object[]> countByTypeForUser(@Param("userId") Long userId);
}
