package com.sports.venue.model;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.SportType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Match implements Serializable {
    private String id;
    private String bookingTeamName;
    private String contactPerson;
    private String contactPhone;
    private SportType sportType;
    private int participantCount;
    private String stadiumId;
    private String courtId;
    private String refereeId;
    private LocalDate matchDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime actualEndTime;
    private int plannedDurationMinutes;
    private boolean isOfficialMatch;
    private MatchStatus status;
    private List<Participant> participants;
    private List<Insurance> insurances;
    private BigDecimal venueFee;
    private BigDecimal refereeFee;
    private BigDecimal overtimeFee;
    private BigDecimal insuranceFee;
    private BigDecimal totalAmount;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancellationReason;
    private String cancellationNote;
    private LocalDateTime cancelledAt;

    public Match() {
        this.id = UUID.randomUUID().toString();
        this.status = MatchStatus.BOOKED;
        this.participants = new ArrayList<>();
        this.insurances = new ArrayList<>();
        this.venueFee = BigDecimal.ZERO;
        this.refereeFee = BigDecimal.ZERO;
        this.overtimeFee = BigDecimal.ZERO;
        this.insuranceFee = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.refundAmount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isOfficialMatch = false;
    }

    @Data
    public static class Participant implements Serializable {
        private String name;
        private String idCard;
        private String phone;

        public Participant() {}

        public Participant(String name, String idCard, String phone) {
            this.name = name;
            this.idCard = idCard;
            this.phone = phone;
        }
    }
}