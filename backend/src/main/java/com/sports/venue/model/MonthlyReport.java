package com.sports.venue.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Data
public class MonthlyReport implements Serializable {
    private String id;
    private YearMonth reportMonth;
    private LocalDate generatedAt;

    private int totalMatches;
    private int completedMatches;
    private int cancelledMatches;
    private double cancellationRate;

    private int totalCourts;
    private int totalAvailableHours;
    private int actualUsedHours;
    private double courtUtilizationRate;

    private BigDecimal totalVenueRevenue;
    private BigDecimal totalRefereeFee;
    private BigDecimal totalOvertimeFee;
    private BigDecimal totalInsuranceFee;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefund;
    private BigDecimal netRevenue;

    public MonthlyReport() {
        this.id = UUID.randomUUID().toString();
        this.totalVenueRevenue = BigDecimal.ZERO;
        this.totalRefereeFee = BigDecimal.ZERO;
        this.totalOvertimeFee = BigDecimal.ZERO;
        this.totalInsuranceFee = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
        this.totalRefund = BigDecimal.ZERO;
        this.netRevenue = BigDecimal.ZERO;
    }
}