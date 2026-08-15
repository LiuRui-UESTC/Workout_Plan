package com.project.dev.code_sports_rui_and_fei.controller;

import com.project.dev.code_sports_rui_and_fei.model.CoupleGoal;
import com.project.dev.code_sports_rui_and_fei.service.CoupleGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class CoupleGoalController {

    private final CoupleGoalService goalService;

    @GetMapping
    public List<CoupleGoal> findAll() {
        return goalService.findAll();
    }

    @GetMapping("/active")
    public List<CoupleGoal> findActive() {
        return goalService.findActive();
    }

    @GetMapping("/{id}")
    public CoupleGoal findById(@PathVariable Long id) {
        return goalService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CoupleGoal> create(@RequestBody CoupleGoal goal) {
        return ResponseEntity.ok(goalService.create(goal));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<CoupleGoal> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Double> payload) {
        Double currentValue = payload.get("currentValue");
        return ResponseEntity.ok(goalService.updateProgress(id, currentValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        goalService.delete(id);
        return ResponseEntity.ok().build();
    }
}
