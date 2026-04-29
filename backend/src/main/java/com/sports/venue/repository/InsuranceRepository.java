package com.sports.venue.repository;

import com.sports.venue.model.Insurance;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InsuranceRepository {

    private final Map<String, Insurance> insurances = new ConcurrentHashMap<>();

    public Insurance save(Insurance insurance) {
        insurances.put(insurance.getId(), insurance);
        return insurance;
    }

    public Optional<Insurance> findById(String id) {
        return Optional.ofNullable(insurances.get(id));
    }

    public List<Insurance> findAll() {
        return new ArrayList<>(insurances.values());
    }

    public void deleteById(String id) {
        insurances.remove(id);
    }

    public boolean existsById(String id) {
        return insurances.containsKey(id);
    }

    public List<Insurance> findByMatchId(String matchId) {
        return insurances.values().stream()
                .filter(i -> matchId.equals(i.getMatchId()))
                .collect(Collectors.toList());
    }

    public List<Insurance> findByParticipantIdCard(String idCard) {
        return insurances.values().stream()
                .filter(i -> idCard.equals(i.getParticipantIdCard()))
                .collect(Collectors.toList());
    }

    public List<Insurance> findActiveInsuranceByDate(LocalDate date) {
        return insurances.values().stream()
                .filter(Insurance::isActive)
                .filter(i -> !date.isBefore(i.getStartDate()) && !date.isAfter(i.getEndDate()))
                .collect(Collectors.toList());
    }

    public void clear() {
        insurances.clear();
    }
}