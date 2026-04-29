package com.sports.venue.repository;

import com.sports.venue.model.Stadium;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class StadiumRepository {

    private final Map<String, Stadium> stadiums = new ConcurrentHashMap<>();

    public Stadium save(Stadium stadium) {
        stadiums.put(stadium.getId(), stadium);
        return stadium;
    }

    public Optional<Stadium> findById(String id) {
        return Optional.ofNullable(stadiums.get(id));
    }

    public List<Stadium> findAll() {
        return new ArrayList<>(stadiums.values());
    }

    public void deleteById(String id) {
        stadiums.remove(id);
    }

    public boolean existsById(String id) {
        return stadiums.containsKey(id);
    }

    public List<Stadium> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lowerKeyword = keyword.toLowerCase();
        return stadiums.values().stream()
                .filter(s -> s.getName().toLowerCase().contains(lowerKeyword)
                        || s.getAddress().toLowerCase().contains(lowerKeyword)
                        || s.getDescription() != null && s.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    public void clear() {
        stadiums.clear();
    }
}