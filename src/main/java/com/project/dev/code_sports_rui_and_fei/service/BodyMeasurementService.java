package com.project.dev.code_sports_rui_and_fei.service;

import com.project.dev.code_sports_rui_and_fei.model.BodyMeasurement;
import com.project.dev.code_sports_rui_and_fei.repository.BodyMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BodyMeasurementService {

    private final BodyMeasurementRepository measurementRepository;

    public List<BodyMeasurement> findByUserId(Long userId) {
        return measurementRepository.findByUserIdOrderByRecordDateDesc(userId);
    }

    public List<BodyMeasurement> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
        return measurementRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, end);
    }

    public Optional<BodyMeasurement> findLatest(Long userId) {
        return measurementRepository.findTopByUserIdOrderByRecordDateDesc(userId);
    }

    @Transactional
    public BodyMeasurement create(BodyMeasurement measurement) {
        return measurementRepository.save(measurement);
    }

    @Transactional
    public void delete(Long id) {
        measurementRepository.deleteById(id);
    }
}
