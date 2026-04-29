package com.sports.venue.repository;

import com.sports.venue.model.FeeRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class FeeRecordRepository {

    private final Map<String, FeeRecord> feeRecords = new ConcurrentHashMap<>();

    public FeeRecord save(FeeRecord record) {
        feeRecords.put(record.getId(), record);
        return record;
    }

    public Optional<FeeRecord> findById(String id) {
        return Optional.ofNullable(feeRecords.get(id));
    }

    public List<FeeRecord> findAll() {
        return new ArrayList<>(feeRecords.values());
    }

    public void deleteById(String id) {
        feeRecords.remove(id);
    }

    public boolean existsById(String id) {
        return feeRecords.containsKey(id);
    }

    public List<FeeRecord> findByMatchId(String matchId) {
        return feeRecords.values().stream()
                .filter(r -> matchId.equals(r.getMatchId()))
                .collect(Collectors.toList());
    }

    public List<FeeRecord> findByFeeType(FeeRecord.FeeType feeType) {
        return feeRecords.values().stream()
                .filter(r -> feeType.equals(r.getFeeType()))
                .collect(Collectors.toList());
    }

    public List<FeeRecord> findByDate(LocalDate date) {
        return feeRecords.values().stream()
                .filter(r -> date.equals(r.getFeeDate()))
                .collect(Collectors.toList());
    }

    public List<FeeRecord> findByMonth(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        return feeRecords.values().stream()
                .filter(r -> !r.getFeeDate().isBefore(startDate) && !r.getFeeDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public List<FeeRecord> findByMonthAndType(YearMonth month, FeeRecord.FeeType feeType) {
        return findByMonth(month).stream()
                .filter(r -> feeType.equals(r.getFeeType()))
                .collect(Collectors.toList());
    }

    public void clear() {
        feeRecords.clear();
    }
}