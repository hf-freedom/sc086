package com.sports.venue.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FeeRecord implements Serializable {
    private String id;
    private String matchId;
    private String refereeId;
    private String courtId;
    private FeeType feeType;
    private BigDecimal amount;
    private LocalDate feeDate;
    private String description;
    private LocalDateTime createdAt;
    private boolean settled;

    public FeeRecord() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.settled = false;
        this.amount = BigDecimal.ZERO;
    }

    public enum FeeType {
        VENUE_RENT("场地租金"),
        REFEREE_FEE("裁判费用"),
        OVERTIME_FEE("超时费用"),
        INSURANCE_FEE("保险费用"),
        REFUND("退款");

        private final String description;

        FeeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}