package com.project.dev.code_sports_rui_and_fei.service;

import com.project.dev.code_sports_rui_and_fei.model.ExerciseRecord;
import com.project.dev.code_sports_rui_and_fei.model.Workout;
import com.project.dev.code_sports_rui_and_fei.model.WorkoutType;
import com.project.dev.code_sports_rui_and_fei.repository.WorkoutRepository;
import com.project.dev.code_sports_rui_and_fei.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public List<Workout> findAll() {
        return workoutRepository.findAll();
    }

    public Workout findById(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健身记录不存在: " + id));
    }

    public List<Workout> findByUserId(Long userId) {
        return workoutRepository.findByUserIdOrderByWorkoutDateDesc(userId);
    }

    public List<Workout> findByDateRange(LocalDate start, LocalDate end) {
        return workoutRepository.findByWorkoutDateBetweenOrderByWorkoutDateDesc(start, end);
    }

    public List<Workout> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
        return workoutRepository.findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(userId, start, end);
    }

    @Transactional
    public Workout create(Workout workout) {
        return workoutRepository.save(workout);
    }

    @Transactional
    public Workout addExercise(Long workoutId, ExerciseRecord record) {
        Workout workout = findById(workoutId);
        record.setWorkout(workout);
        workout.getExercises().add(record);
        return workoutRepository.save(workout);
    }

    @Transactional
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getStats(Long userId, LocalDate start, LocalDate end) {
        Map<String, Object> stats = new LinkedHashMap<>();

        long workoutCount = workoutRepository.countByUserIdAndDateRange(userId, start, end);
        Double totalCalories = workoutRepository.sumCaloriesByUserIdAndDateRange(userId, start, end);
        Integer totalDuration = workoutRepository.sumDurationByUserIdAndDateRange(userId, start, end);
        Double totalDistance = workoutRepository.sumDistanceByUserIdAndDateRange(userId, start, end);

        // 本周连续打卡天数
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate today = LocalDate.now();
        int streakDays = 0;
        for (LocalDate d = today; !d.isBefore(weekStart); d = d.minusDays(1)) {
            long count = workoutRepository.countByUserIdAndDateRange(userId, d, d);
            if (count > 0) {
                streakDays++;
            } else {
                break;
            }
        }

        // 按运动类型统计
        List<Object[]> typeStats = workoutRepository.countByTypeForUser(userId);
        List<Map<String, Object>> typeDistribution = typeStats.stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", ((WorkoutType) row[0]).name());
            item.put("displayName", ((WorkoutType) row[0]).getDisplayName());
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());

        stats.put("workoutCount", workoutCount);
        stats.put("totalCalories", totalCalories != null ? totalCalories : 0.0);
        stats.put("totalDuration", totalDuration != null ? totalDuration : 0);
        stats.put("totalDistance", totalDistance != null ? totalDistance : 0.0);
        stats.put("weekStreakDays", streakDays);
        stats.put("typeDistribution", typeDistribution);
        stats.put("startDate", start.toString());
        stats.put("endDate", end.toString());

        return stats;
    }

    /**
     * 获取双人对比统计
     */
    public Map<String, Object> getCoupleStats(Long user1Id, Long user2Id, LocalDate start, LocalDate end) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user1", getStats(user1Id, start, end));
        result.put("user2", getStats(user2Id, start, end));
        return result;
    }
}
