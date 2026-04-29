package com.sports.venue.enums;

public enum WeatherCondition {
    SUNNY("晴天", false),
    CLOUDY("多云", false),
    RAINY("下雨", true),
    STORMY("暴风雨", true),
    SNOWY("下雪", true),
    FOGGY("大雾", true),
    EXTREME_HEAT("极端高温", true),
    EXTREME_COLD("极端低温", true);

    private final String description;
    private final boolean isAbnormal;

    WeatherCondition(String description, boolean isAbnormal) {
        this.description = description;
        this.isAbnormal = isAbnormal;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAbnormal() {
        return isAbnormal;
    }
}