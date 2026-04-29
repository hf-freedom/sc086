package com.sports.venue.model;

import com.sports.venue.enums.SportType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class Referee implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String idCard;
    private List<SportType> qualifiedSports;
    private String qualificationLevel;
    private BigDecimal hourlyRate;
    private Map<DayOfWeek, List<TimeSlot>> availableHours;
    private List<LeaveRecord> leaveRecords;
    private boolean active;
    private LocalDateTime createdAt;

    public Referee() {
        this.id = UUID.randomUUID().toString();
        this.qualifiedSports = new ArrayList<>();
        this.availableHours = new HashMap<>();
        this.leaveRecords = new ArrayList<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.hourlyRate = BigDecimal.ZERO;
    }

    public Referee(String name, String phone, String qualificationLevel) {
        this();
        this.name = name;
        this.phone = phone;
        this.qualificationLevel = qualificationLevel;
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

    @Data
    public static class LeaveRecord implements Serializable {
        private LocalDate leaveDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
        private LocalDateTime createdAt;
        private boolean approved;

        public LeaveRecord() {
            this.createdAt = LocalDateTime.now();
            this.approved = false;
        }
    }
}