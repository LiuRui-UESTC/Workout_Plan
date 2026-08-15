package com.project.dev.code_sports_rui_and_fei.repository;

import com.project.dev.code_sports_rui_and_fei.model.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {

    List<BodyMeasurement> findByUserIdOrderByRecordDateDesc(Long userId);

    List<BodyMeasurement> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            Long userId, LocalDate start, LocalDate end);

    Optional<BodyMeasurement> findTopByUserIdOrderByRecordDateDesc(Long userId);
}
