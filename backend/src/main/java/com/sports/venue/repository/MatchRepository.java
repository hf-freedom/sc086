package com.sports.venue.repository;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.SportType;
import com.sports.venue.model.Match;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MatchRepository {

    private final Map<String, Match> matches = new ConcurrentHashMap<>();

    public Match save(Match match) {
        match.setUpdatedAt(java.time.LocalDateTime.now());
        matches.put(match.getId(), match);
        return match;
    }

    public Optional<Match> findById(String id) {
        return Optional.ofNullable(matches.get(id));
    }

    public List<Match> findAll() {
        return new ArrayList<>(matches.values());
    }

    public void deleteById(String id) {
        matches.remove(id);
    }

    public boolean existsById(String id) {
        return matches.containsKey(id);
    }

    public List<Match> findByCourtId(String courtId) {
        return matches.values().stream()
                .filter(m -> courtId.equals(m.getCourtId()))
                .collect(Collectors.toList());
    }

    public List<Match> findByRefereeId(String refereeId) {
        return matches.values().stream()
                .filter(m -> refereeId.equals(m.getRefereeId()))
                .collect(Collectors.toList());
    }

    public List<Match> findByStatus(MatchStatus status) {
        return matches.values().stream()
                .filter(m -> status.equals(m.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Match> findByMatchDate(LocalDate date) {
        return matches.values().stream()
                .filter(m -> date.equals(m.getMatchDate()))
                .collect(Collectors.toList());
    }

    public List<Match> findByMatchDateBetween(LocalDate startDate, LocalDate endDate) {
        return matches.values().stream()
                .filter(m -> !m.getMatchDate().isBefore(startDate) && !m.getMatchDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public List<Match> findConflictingMatches(String courtId, LocalDate matchDate, LocalTime startTime, LocalTime endTime, String excludeMatchId) {
        return matches.values().stream()
                .filter(m -> courtId.equals(m.getCourtId()))
                .filter(m -> matchDate.equals(m.getMatchDate()))
                .filter(m -> !MatchStatus.CANCELLED.equals(m.getStatus()))
                .filter(m -> excludeMatchId == null || !excludeMatchId.equals(m.getId()))
                .filter(m -> hasTimeOverlap(m.getStartTime(), m.getEndTime(), startTime, endTime))
                .collect(Collectors.toList());
    }

    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    public List<Match> findBySportTypeAndStatus(SportType sportType, MatchStatus status) {
        return matches.values().stream()
                .filter(m -> sportType.equals(m.getSportType()))
                .filter(m -> status.equals(m.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Match> findByMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        return findByMatchDateBetween(startOfMonth, endOfMonth);
    }

    public void clear() {
        matches.clear();
    }
}