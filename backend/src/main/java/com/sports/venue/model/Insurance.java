package com.sports.venue.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Insurance implements Serializable {
    private String id;
    private String matchId;
    private String participantName;
    private String participantIdCard;
    private String participantPhone;
    private BigDecimal premium;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String insuranceType;
    private String policyNumber;
    private LocalDateTime purchasedAt;
    private boolean active;

    public Insurance() {
        this.id = UUID.randomUUID().toString();
        this.purchasedAt = LocalDateTime.now();
        this.active = true;
        this.premium = BigDecimal.ZERO;
        this.coverageAmount = BigDecimal.ZERO;
    }
}