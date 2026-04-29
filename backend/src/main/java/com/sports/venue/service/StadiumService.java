package com.sports.venue.service;

import com.sports.venue.enums.SportType;
import com.sports.venue.enums.VenueType;
import com.sports.venue.model.Court;
import com.sports.venue.model.Stadium;
import com.sports.venue.repository.CourtRepository;
import com.sports.venue.repository.StadiumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class StadiumService {

    private static final Logger logger = LoggerFactory.getLogger(StadiumService.class);

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private CourtRepository courtRepository;

    public Stadium createStadium(String name, String address, VenueType venueType) {
        Stadium stadium = new Stadium(name, address, venueType);
        return stadiumRepository.save(stadium);
    }

    public Optional<Stadium> getStadiumById(String id) {
        return stadiumRepository.findById(id);
    }

    public List<Stadium> getAllStadiums() {
        return stadiumRepository.findAll();
    }

    public Stadium updateStadium(String id, String name, String address, VenueType venueType) {
        Optional<Stadium> stadiumOpt = stadiumRepository.findById(id);
        if (!stadiumOpt.isPresent()) {
            throw new RuntimeException("场馆不存在");
        }
        Stadium stadium = stadiumOpt.get();
        if (name != null) stadium.setName(name);
        if (address != null) stadium.setAddress(address);
        if (venueType != null) stadium.setVenueType(venueType);
        return stadiumRepository.save(stadium);
    }

    public void deleteStadium(String id) {
        List<Court> courts = courtRepository.findByStadiumId(id);
        for (Court court : courts) {
            court.setActive(false);
            courtRepository.save(court);
        }
        stadiumRepository.deleteById(id);
    }

    @Transactional
    public Court addCourt(String stadiumId, String name, String code, 
                          List<SportType> supportedSports, int capacity,
                          java.math.BigDecimal hourlyRate, java.math.BigDecimal overtimeRate) {
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new RuntimeException("场馆不存在");
        }
        
        Court court = new Court();
        court.setStadiumId(stadiumId);
        court.setName(name);
        court.setCode(code);
        court.setSupportedSports(supportedSports);
        court.setCapacity(capacity);
        court.setHourlyRate(hourlyRate);
        court.setOvertimeRate(overtimeRate != null ? overtimeRate : hourlyRate);
        
        for (DayOfWeek day : DayOfWeek.values()) {
            court.getAvailableHours().put(day, new Court.TimeSlot(LocalTime.of(8, 0), LocalTime.of(22, 0)));
        }
        
        return courtRepository.save(court);
    }

    public Optional<Court> getCourtById(String id) {
        return courtRepository.findById(id);
    }

    public List<Court> getCourtsByStadium(String stadiumId) {
        return courtRepository.findByStadiumId(stadiumId);
    }

    public List<Court> getCourtsBySportType(SportType sportType) {
        return courtRepository.findBySportType(sportType);
    }

    public List<Court> findAvailableCourts(String stadiumId, SportType sportType) {
        if (stadiumId != null) {
            return courtRepository.findByStadiumIdAndSportType(stadiumId, sportType);
        }
        return courtRepository.findBySportType(sportType);
    }

    public Court updateCourt(String id, String name, String code,
                             List<SportType> supportedSports, Integer capacity,
                             java.math.BigDecimal hourlyRate, java.math.BigDecimal overtimeRate) {
        Optional<Court> courtOpt = courtRepository.findById(id);
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("场地不存在");
        }
        Court court = courtOpt.get();
        if (name != null) court.setName(name);
        if (code != null) court.setCode(code);
        if (supportedSports != null) court.setSupportedSports(supportedSports);
        if (capacity != null) court.setCapacity(capacity);
        if (hourlyRate != null) court.setHourlyRate(hourlyRate);
        if (overtimeRate != null) court.setOvertimeRate(overtimeRate);
        return courtRepository.save(court);
    }

    public void setCourtActive(String id, boolean active) {
        Optional<Court> courtOpt = courtRepository.findById(id);
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("场地不存在");
        }
        Court court = courtOpt.get();
        court.setActive(active);
        courtRepository.save(court);
    }

    public void updateCourtAvailableHours(String courtId, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("场地不存在");
        }
        Court court = courtOpt.get();
        court.getAvailableHours().put(day, new Court.TimeSlot(startTime, endTime));
        courtRepository.save(court);
    }

    public Court.TimeSlot getCourtAvailableHours(String courtId, DayOfWeek day) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("场地不存在");
        }
        return courtOpt.get().getAvailableHours().get(day);
    }
}