package com.project.dev.code_sports_rui_and_fei.service;

import com.project.dev.code_sports_rui_and_fei.model.CoupleGoal;
import com.project.dev.code_sports_rui_and_fei.repository.CoupleGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoupleGoalService {

    private final CoupleGoalRepository goalRepository;

    public List<CoupleGoal> findAll() {
        return goalRepository.findAllByOrderByStartDateDesc();
    }

    public List<CoupleGoal> findActive() {
        return goalRepository.findByCompletedFalse();
    }

    public CoupleGoal findById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("目标不存在: " + id));
    }

    @Transactional
    public CoupleGoal create(CoupleGoal goal) {
        return goalRepository.save(goal);
    }

    @Transactional
    public CoupleGoal updateProgress(Long id, Double currentValue) {
        CoupleGoal goal = findById(id);
        goal.setCurrentValue(currentValue);
        if (goal.getTargetValue() != null && currentValue >= goal.getTargetValue()) {
            goal.setCompleted(true);
        }
        return goalRepository.save(goal);
    }

    @Transactional
    public void delete(Long id) {
        goalRepository.deleteById(id);
    }
}
