package com.project.dev.code_sports_rui_and_fei.repository;

import com.project.dev.code_sports_rui_and_fei.model.CoupleGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoupleGoalRepository extends JpaRepository<CoupleGoal, Long> {

    List<CoupleGoal> findByCompletedFalse();
    List<CoupleGoal> findAllByOrderByStartDateDesc();
}
