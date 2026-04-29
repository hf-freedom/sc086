package com.sports.venue.controller;

import com.sports.venue.enums.SportType;
import com.sports.venue.model.Referee;
import com.sports.venue.service.RefereeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/referees")
@CrossOrigin(origins = "*")
public class RefereeController {

    @Autowired
    private RefereeService refereeService;

    @GetMapping
    public List<Referee> getAllReferees(@RequestParam(required = false) Boolean active,
                                          @RequestParam(required = false) SportType sportType) {
        if (Boolean.TRUE.equals(active)) {
            if (sportType != null) {
                return refereeService.getRefereesBySportType(sportType);
            }
            return refereeService.getActiveReferees();
        }
        return refereeService.getAllReferees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Referee> getRefereeById(@PathVariable String id) {
        Optional<Referee> referee = refereeService.getRefereeById(id);
        return referee.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Referee createReferee(@RequestParam String name,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String idCard,
                                  @RequestParam List<SportType> qualifiedSports,
                                  @RequestParam String qualificationLevel,
                                  @RequestParam BigDecimal hourlyRate) {
        return refereeService.createReferee(name, phone, idCard, qualifiedSports, qualificationLevel, hourlyRate);
    }

    @PutMapping("/{id}")
    public Referee updateReferee(@PathVariable String id,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) List<SportType> qualifiedSports,
                                  @RequestParam(required = false) String qualificationLevel,
                                  @RequestParam(required = false) BigDecimal hourlyRate) {
        return refereeService.updateReferee(id, name, phone, qualifiedSports, qualificationLevel, hourlyRate);
    }

    @PutMapping("/{id}/active")
    public void setRefereeActive(@PathVariable String id, @RequestParam boolean active) {
        refereeService.setRefereeActive(id, active);
    }

    @GetMapping("/{id}/slots/{day}")
    public List<Referee.TimeSlot> getAvailableSlots(@PathVariable String id, @PathVariable DayOfWeek day) {
        return refereeService.getRefereeAvailableSlots(id, day);
    }

    @PostMapping("/{id}/leave")
    public Referee.LeaveRecord submitLeave(@PathVariable String id,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate leaveDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                             @RequestParam String reason) {
        return refereeService.submitLeaveRequest(id, leaveDate, startTime, endTime, reason);
    }

    @PostMapping("/{id}/leave/{index}/approve")
    public void approveLeave(@PathVariable String id, @PathVariable int index) {
        refereeService.approveLeaveRequest(id, index);
    }
}