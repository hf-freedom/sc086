package com.sports.venue.repository;

import com.sports.venue.enums.SportType;
import com.sports.venue.model.Court;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class CourtRepository {

    private final Map<String, Court> courts = new ConcurrentHashMap<>();

    public Court save(Court court) {
        courts.put(court.getId(), court);
        return court;
    }

    public Optional<Court> findById(String id) {
        return Optional.ofNullable(courts.get(id));
    }

    public List<Court> findAll() {
        return new ArrayList<>(courts.values());
    }

    public void deleteById(String id) {
        courts.remove(id);
    }

    public boolean existsById(String id) {
        return courts.containsKey(id);
    }

    public List<Court> findByStadiumId(String stadiumId) {
        return courts.values().stream()
                .filter(c -> stadiumId.equals(c.getStadiumId()))
                .collect(Collectors.toList());
    }

    public List<Court> findBySportType(SportType sportType) {
        if (sportType == null) {
            return findActiveCourts();
        }
        return courts.values().stream()
                .filter(Court::isActive)
                .filter(c -> c.getSupportedSports() != null && c.getSupportedSports().contains(sportType))
                .collect(Collectors.toList());
    }

    public List<Court> findActiveCourts() {
        return courts.values().stream()
                .filter(Court::isActive)
                .collect(Collectors.toList());
    }

    public List<Court> findByStadiumIdAndSportType(String stadiumId, SportType sportType) {
        if (sportType == null) {
            return courts.values().stream()
                    .filter(Court::isActive)
                    .filter(c -> stadiumId.equals(c.getStadiumId()))
                    .collect(Collectors.toList());
        }
        return courts.values().stream()
                .filter(Court::isActive)
                .filter(c -> stadiumId.equals(c.getStadiumId()))
                .filter(c -> c.getSupportedSports() != null && c.getSupportedSports().contains(sportType))
                .collect(Collectors.toList());
    }

    public void clear() {
        courts.clear();
    }
}