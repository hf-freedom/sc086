package com.sports.venue.controller;

import com.sports.venue.enums.WeatherCondition;
import com.sports.venue.model.WeatherRecord;
import com.sports.venue.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/current")
    public WeatherRecord getCurrentWeather(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return weatherService.getCurrentWeather(date);
    }

    @GetMapping("/history")
    public List<WeatherRecord> getWeatherHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return weatherService.getWeatherHistory(startDate, endDate);
    }

    @GetMapping("/abnormal")
    public boolean isWeatherAbnormal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return weatherService.isWeatherAbnormal(date);
    }

    @PostMapping("/record")
    public WeatherRecord recordManualWeather(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam WeatherCondition condition,
            @RequestParam String description,
            @RequestParam double temperature,
            @RequestParam double humidity,
            @RequestParam String windDirection,
            @RequestParam double windSpeed) {
        return weatherService.recordManualWeather(date, condition, description, 
                temperature, humidity, windDirection, windSpeed);
    }
}