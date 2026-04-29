package com.sports.venue.repository;

import com.sports.venue.enums.SportType;
import com.sports.venue.model.Referee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class RefereeRepository {

    private final Map<String, Referee> referees = new ConcurrentHashMap<>();

    public Referee save(Referee referee) {
        referees.put(referee.getId(), referee);
        return referee;
    }

    public Optional<Referee> findById(String id) {
        return Optional.ofNullable(referees.get(id));
    }

    public List<Referee> findAll() {
        return new ArrayList<>(referees.values());
    }

    public void deleteById(String id) {
        referees.remove(id);
    }

    public boolean existsById(String id) {
        return referees.containsKey(id);
    }

    public List<Referee> findActiveReferees() {
        return referees.values().stream()
                .filter(Referee::isActive)
                .collect(Collectors.toList());
    }

    public List<Referee> findByQualifiedSport(SportType sportType) {
        return referees.values().stream()
                .filter(r -> r.getQualifiedSports() != null && r.getQualifiedSports().contains(sportType))
                .filter(Referee::isActive)
                .collect(Collectors.toList());
    }

    public List<Referee> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lowerKeyword = keyword.toLowerCase();
        return referees.values().stream()
                .filter(r -> r.getName().toLowerCase().contains(lowerKeyword)
                        || r.getPhone().contains(lowerKeyword)
                        || r.getQualificationLevel().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public void clear() {
        referees.clear();
    }
}