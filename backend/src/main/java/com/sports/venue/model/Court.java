package com.sports.venue.model;

import com.sports.venue.enums.SportType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class Court implements Serializable {
    private String id;
    private String stadiumId;
    private String name;
    private String code;
    private List<SportType> supportedSports;
    private int capacity;
    private BigDecimal hourlyRate;
    private BigDecimal overtimeRate;
    private Map<DayOfWeek, TimeSlot> availableHours;
    private boolean active;

    public Court() {
        this.id = UUID.randomUUID().toString();
        this.supportedSports = new ArrayList<>();
        this.availableHours = new HashMap<>();
        this.active = true;
        this.overtimeRate = BigDecimal.ZERO;
    }

    @Data
    public static class TimeSlot implements Serializable {
        private LocalTime startTime;
        private LocalTime endTime;

        public TimeSlot() {}

        public TimeSlot(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}