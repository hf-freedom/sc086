package com.sports.venue.enums;

public enum CancellationReason {
    WEATHER("天气原因"),
    TEAM_CANCEL("团队取消"),
    REFEREE_LEAVE("裁判请假"),
    VENUE_MAINTENANCE("场馆维护"),
    OTHER("其他原因");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}