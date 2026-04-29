package com.sports.venue.model;

import com.sports.venue.enums.WeatherCondition;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WeatherRecord implements Serializable {
    private String id;
    private LocalDate recordDate;
    private WeatherCondition condition;
    private String description;
    private double temperature;
    private double humidity;
    private String windDirection;
    private double windSpeed;
    private LocalDateTime recordedAt;
    private String source;

    public WeatherRecord() {
        this.id = UUID.randomUUID().toString();
        this.recordedAt = LocalDateTime.now();
    }

    public boolean isAbnormal() {
        return this.condition != null && this.condition.isAbnormal();
    }
}