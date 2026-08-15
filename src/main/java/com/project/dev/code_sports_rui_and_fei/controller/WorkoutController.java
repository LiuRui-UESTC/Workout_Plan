package com.project.dev.code_sports_rui_and_fei.controller;

import com.project.dev.code_sports_rui_and_fei.model.ExerciseRecord;
import com.project.dev.code_sports_rui_and_fei.model.Workout;
import com.project.dev.code_sports_rui_and_fei.model.WorkoutType;
import com.project.dev.code_sports_rui_and_fei.service.UserService;
import com.project.dev.code_sports_rui_and_fei.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;

    @GetMapping
    public List<Workout> findAll() {
        return workoutService.findAll();
    }

    @GetMapping("/{id}")
    public Workout findById(@PathVariable Long id) {
        return workoutService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Workout> findByUser(@PathVariable Long userId) {
        return workoutService.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/range")
    public List<Workout> findByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return workoutService.findByUserIdAndDateRange(userId, start, end);
    }

    @PostMapping
    public ResponseEntity<Workout> create(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Workout workout = Workout.builder()
                .user(userService.findById(userId))
                .workoutType(WorkoutType.valueOf(payload.get("workoutType").toString()))
                .workoutDate(LocalDate.parse(payload.get("workoutDate").toString()))
                .durationMinutes(payload.containsKey("durationMinutes") ? Integer.valueOf(payload.get("durationMinutes").toString()) : null)
                .caloriesBurned(payload.containsKey("caloriesBurned") ? Double.valueOf(payload.get("caloriesBurned").toString()) : null)
                .distance(payload.containsKey("distance") ? Double.valueOf(payload.get("distance").toString()) : null)
                .notes(payload.containsKey("notes") ? payload.get("notes").toString() : null)
                .moodRating(payload.containsKey("moodRating") ? Integer.valueOf(payload.get("moodRating").toString()) : null)
                .build();
        return ResponseEntity.ok(workoutService.create(workout));
    }

    @PostMapping("/{workoutId}/exercises")
    public ResponseEntity<Workout> addExercise(@PathVariable Long workoutId, @RequestBody ExerciseRecord record) {
        return ResponseEntity.ok(workoutService.addExercise(workoutId, record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/types")
    public Map<String, String> getWorkoutTypes() {
        Map<String, String> types = new LinkedHashMap<>();
        for (WorkoutType type : WorkoutType.values()) {
            types.put(type.name(), type.getDisplayName());
        }
        return types;
    }

    @GetMapping("/stats/user/{userId}")
    public Map<String, Object> getStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return workoutService.getStats(userId, start, end);
    }

    @GetMapping("/stats/couple")
    public Map<String, Object> getCoupleStats(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return workoutService.getCoupleStats(user1Id, user2Id, start, end);
    }
}
