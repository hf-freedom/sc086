package com.sports.venue.repository;

import com.sports.venue.model.MonthlyReport;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MonthlyReportRepository {

    private final Map<String, MonthlyReport> reports = new ConcurrentHashMap<>();

    public MonthlyReport save(MonthlyReport report) {
        reports.put(report.getId(), report);
        return report;
    }

    public Optional<MonthlyReport> findById(String id) {
        return Optional.ofNullable(reports.get(id));
    }

    public List<MonthlyReport> findAll() {
        return new ArrayList<>(reports.values());
    }

    public void deleteById(String id) {
        reports.remove(id);
    }

    public boolean existsById(String id) {
        return reports.containsKey(id);
    }

    public Optional<MonthlyReport> findByMonth(YearMonth month) {
        return reports.values().stream()
                .filter(r -> month.equals(r.getReportMonth()))
                .findFirst();
    }

    public List<MonthlyReport> findByYear(int year) {
        return reports.values().stream()
                .filter(r -> r.getReportMonth().getYear() == year)
                .sorted((r1, r2) -> r2.getReportMonth().compareTo(r1.getReportMonth()))
                .collect(Collectors.toList());
    }

    public void clear() {
        reports.clear();
    }
}