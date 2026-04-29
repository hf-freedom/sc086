package com.sports.venue.controller;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.SportType;
import com.sports.venue.model.Match;
import com.sports.venue.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping
    public List<Match> getAllMatches(@RequestParam(required = false) MatchStatus status,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (status != null) {
            return matchService.getMatchesByStatus(status);
        }
        if (date != null) {
            return matchService.getMatchesByDate(date);
        }
        return matchService.getAllMatches();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable String id) {
        Optional<Match> match = matchService.getMatchById(id);
        return match.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Match createMatch(@RequestParam String bookingTeamName,
                              @RequestParam String contactPerson,
                              @RequestParam String contactPhone,
                              @RequestParam SportType sportType,
                              @RequestParam int participantCount,
                              @RequestParam String stadiumId,
                              @RequestParam String courtId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate matchDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                              @RequestParam int durationMinutes,
                              @RequestParam(required = false, defaultValue = "false") boolean isOfficialMatch,
                              @RequestBody(required = false) List<Match.Participant> participants) {
        return matchService.createMatch(bookingTeamName, contactPerson, contactPhone,
                sportType, participantCount, stadiumId, courtId,
                matchDate, startTime, durationMinutes, isOfficialMatch, participants);
    }

    @PutMapping("/{id}/status")
    public Match updateMatchStatus(@PathVariable String id, @RequestParam MatchStatus status) {
        return matchService.updateMatchStatus(id, status);
    }

    @PostMapping("/{id}/start")
    public Match startMatch(@PathVariable String id) {
        return matchService.startMatch(id);
    }

    @PostMapping("/{id}/complete")
    public Match completeMatch(@PathVariable String id,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime actualEndTime) {
        return matchService.completeMatch(id, actualEndTime);
    }

    @GetMapping("/court/{courtId}")
    public List<Match> getMatchesByCourt(@PathVariable String courtId) {
        return matchService.getMatchesByCourtId(courtId);
    }

    @GetMapping("/referee/{refereeId}")
    public List<Match> getMatchesByReferee(@PathVariable String refereeId) {
        return matchService.getMatchesByRefereeId(refereeId);
    }

    @GetMapping("/monthly")
    public List<Match> getMatchesByMonth(@RequestParam int year, @RequestParam int month) {
        return matchService.getMatchesByMonth(year, month);
    }

    @PostMapping("/{id}/confirm")
    public Match confirmMatch(@PathVariable String id) {
        return matchService.confirmMatch(id);
    }

    @PostMapping("/{id}/reassign-referee")
    public Match reassignReferee(@PathVariable String id) {
        return matchService.reassignReferee(id);
    }
}