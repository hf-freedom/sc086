package com.sports.venue.repository;

import com.sports.venue.model.WeatherRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WeatherRecordRepository {

    private final Map<String, WeatherRecord> weatherRecords = new ConcurrentHashMap<>();

    public WeatherRecord save(WeatherRecord record) {
        weatherRecords.put(record.getId(), record);
        return record;
    }

    public Optional<WeatherRecord> findById(String id) {
        return Optional.ofNullable(weatherRecords.get(id));
    }

    public List<WeatherRecord> findAll() {
        return new ArrayList<>(weatherRecords.values());
    }

    public void deleteById(String id) {
        weatherRecords.remove(id);
    }

    public boolean existsById(String id) {
        return weatherRecords.containsKey(id);
    }

    public Optional<WeatherRecord> findLatestByDate(LocalDate date) {
        return weatherRecords.values().stream()
                .filter(r -> date.equals(r.getRecordDate()))
                .sorted((r1, r2) -> r2.getRecordedAt().compareTo(r1.getRecordedAt()))
                .findFirst();
    }

    public List<WeatherRecord> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return weatherRecords.values().stream()
                .filter(r -> !r.getRecordDate().isBefore(startDate) && !r.getRecordDate().isAfter(endDate))
                .sorted((r1, r2) -> r2.getRecordedAt().compareTo(r1.getRecordedAt()))
                .collect(Collectors.toList());
    }

    public List<WeatherRecord> findAbnormalWeatherByDate(LocalDate date) {
        return weatherRecords.values().stream()
                .filter(r -> date.equals(r.getRecordDate()))
                .filter(WeatherRecord::isAbnormal)
                .collect(Collectors.toList());
    }

    public void clear() {
        weatherRecords.clear();
    }
}