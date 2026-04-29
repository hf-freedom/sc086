package com.sports.venue.controller;

import com.sports.venue.enums.SportType;
import com.sports.venue.enums.VenueType;
import com.sports.venue.model.Court;
import com.sports.venue.model.Stadium;
import com.sports.venue.service.StadiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stadiums")
@CrossOrigin(origins = "*")
public class StadiumController {

    @Autowired
    private StadiumService stadiumService;

    @GetMapping
    public List<Stadium> getAllStadiums() {
        return stadiumService.getAllStadiums();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stadium> getStadiumById(@PathVariable String id) {
        Optional<Stadium> stadium = stadiumService.getStadiumById(id);
        return stadium.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Stadium createStadium(@RequestParam String name,
                                  @RequestParam String address,
                                  @RequestParam VenueType venueType) {
        return stadiumService.createStadium(name, address, venueType);
    }

    @PutMapping("/{id}")
    public Stadium updateStadium(@PathVariable String id,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) String address,
                                  @RequestParam(required = false) VenueType venueType) {
        return stadiumService.updateStadium(id, name, address, venueType);
    }

    @DeleteMapping("/{id}")
    public void deleteStadium(@PathVariable String id) {
        stadiumService.deleteStadium(id);
    }

    @GetMapping("/{stadiumId}/courts")
    public List<Court> getCourtsByStadium(@PathVariable String stadiumId) {
        return stadiumService.getCourtsByStadium(stadiumId);
    }

    @PostMapping("/{stadiumId}/courts")
    public Court addCourt(@PathVariable String stadiumId,
                          @RequestParam String name,
                          @RequestParam String code,
                          @RequestParam List<SportType> supportedSports,
                          @RequestParam int capacity,
                          @RequestParam BigDecimal hourlyRate,
                          @RequestParam(required = false) BigDecimal overtimeRate) {
        return stadiumService.addCourt(stadiumId, name, code, supportedSports, capacity, hourlyRate, overtimeRate);
    }

    @GetMapping("/courts/{courtId}")
    public ResponseEntity<Court> getCourtById(@PathVariable String courtId) {
        Optional<Court> court = stadiumService.getCourtById(courtId);
        return court.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/courts")
    public List<Court> getCourtsBySportType(@RequestParam(required = false) SportType sportType,
                                              @RequestParam(required = false) String stadiumId) {
        if (sportType != null) {
            if (stadiumId != null) {
                return stadiumService.findAvailableCourts(stadiumId, sportType);
            }
            return stadiumService.getCourtsBySportType(sportType);
        }
        return stadiumService.findAvailableCourts(stadiumId, null);
    }

    @PutMapping("/courts/{courtId}")
    public Court updateCourt(@PathVariable String courtId,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) String code,
                             @RequestParam(required = false) List<SportType> supportedSports,
                             @RequestParam(required = false) Integer capacity,
                             @RequestParam(required = false) BigDecimal hourlyRate,
                             @RequestParam(required = false) BigDecimal overtimeRate) {
        return stadiumService.updateCourt(courtId, name, code, supportedSports, capacity, hourlyRate, overtimeRate);
    }

    @PutMapping("/courts/{courtId}/active")
    public void setCourtActive(@PathVariable String courtId, @RequestParam boolean active) {
        stadiumService.setCourtActive(courtId, active);
    }

    @GetMapping("/courts/{courtId}/hours/{day}")
    public Court.TimeSlot getCourtHours(@PathVariable String courtId, @PathVariable DayOfWeek day) {
        return stadiumService.getCourtAvailableHours(courtId, day);
    }

    @PutMapping("/courts/{courtId}/hours/{day}")
    public void updateCourtHours(@PathVariable String courtId,
                                  @PathVariable DayOfWeek day,
                                  @RequestParam LocalTime startTime,
                                  @RequestParam LocalTime endTime) {
        stadiumService.updateCourtAvailableHours(courtId, day, startTime, endTime);
    }
}