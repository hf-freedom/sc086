package com.sports.venue.enums;

public enum MatchStatus {
    BOOKED("已预约"),
    PENDING_CONFIRM("待确认"),
    CONFIRMED("已确认"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    RESCHEDULED("已改期");

    private final String description;

    MatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}