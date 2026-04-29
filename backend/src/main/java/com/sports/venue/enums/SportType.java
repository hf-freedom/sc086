package com.sports.venue.enums;

public enum SportType {
    BASKETBALL("篮球"),
    FOOTBALL("足球"),
    TENNIS("网球"),
    BADMINTON("羽毛球"),
    TABLE_TENNIS("乒乓球"),
    VOLLEYBALL("排球"),
    SWIMMING("游泳"),
    GYMNASTICS("体操");

    private final String description;

    SportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}