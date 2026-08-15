package com.project.dev.code_sports_rui_and_fei.controller;

import com.project.dev.code_sports_rui_and_fei.model.BodyMeasurement;
import com.project.dev.code_sports_rui_and_fei.service.BodyMeasurementService;
import com.project.dev.code_sports_rui_and_fei.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class BodyMeasurementController {

    private final BodyMeasurementService measurementService;
    private final UserService userService;

    @GetMapping("/user/{userId}")
    public List<BodyMeasurement> findByUser(@PathVariable Long userId) {
        return measurementService.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/range")
    public List<BodyMeasurement> findByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return measurementService.findByUserIdAndDateRange(userId, start, end);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<BodyMeasurement> findLatest(@PathVariable Long userId) {
        return measurementService.findLatest(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<BodyMeasurement> create(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        BodyMeasurement measurement = BodyMeasurement.builder()
                .user(userService.findById(userId))
                .recordDate(LocalDate.parse(payload.get("recordDate").toString()))
                .weight(payload.containsKey("weight") ? Double.valueOf(payload.get("weight").toString()) : null)
                .bodyFatPercentage(payload.containsKey("bodyFatPercentage") ? Double.valueOf(payload.get("bodyFatPercentage").toString()) : null)
                .chestCircumference(payload.containsKey("chestCircumference") ? Double.valueOf(payload.get("chestCircumference").toString()) : null)
                .waistCircumference(payload.containsKey("waistCircumference") ? Double.valueOf(payload.get("waistCircumference").toString()) : null)
                .hipCircumference(payload.containsKey("hipCircumference") ? Double.valueOf(payload.get("hipCircumference").toString()) : null)
                .armCircumference(payload.containsKey("armCircumference") ? Double.valueOf(payload.get("armCircumference").toString()) : null)
                .thighCircumference(payload.containsKey("thighCircumference") ? Double.valueOf(payload.get("thighCircumference").toString()) : null)
                .notes(payload.containsKey("notes") ? payload.get("notes").toString() : null)
                .build();
        return ResponseEntity.ok(measurementService.create(measurement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        measurementService.delete(id);
        return ResponseEntity.ok().build();
    }
}
