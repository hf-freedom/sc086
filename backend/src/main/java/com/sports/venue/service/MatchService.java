package com.sports.venue.service;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.SportType;
import com.sports.venue.model.Court;
import com.sports.venue.model.Match;
import com.sports.venue.repository.CourtRepository;
import com.sports.venue.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private RefereeService refereeService;

    public Match createMatch(String bookingTeamName, String contactPerson, String contactPhone,
                             SportType sportType, int participantCount,
                             String stadiumId, String courtId,
                             LocalDate matchDate, LocalTime startTime, int durationMinutes,
                             boolean isOfficialMatch, List<Match.Participant> participants) {
        
        logger.info("创建赛事预约: 队伍={}, 运动类型={}, 日期={}, 时间={}", 
                bookingTeamName, sportType, matchDate, startTime);

        if (!courtRepository.existsById(courtId)) {
            throw new RuntimeException("场地不存在");
        }

        Court court = courtRepository.findById(courtId).get();
        
        if (!court.getSupportedSports().contains(sportType)) {
            throw new RuntimeException("该场地不支持" + sportType.getDescription() + "项目");
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        validateTimeAvailability(courtId, matchDate, startTime, endTime, null);

        validateCourtHours(court, matchDate.getDayOfWeek(), startTime, endTime);

        Match match = new Match();
        match.setBookingTeamName(bookingTeamName);
        match.setContactPerson(contactPerson);
        match.setContactPhone(contactPhone);
        match.setSportType(sportType);
        match.setParticipantCount(participantCount);
        match.setStadiumId(stadiumId);
        match.setCourtId(courtId);
        match.setMatchDate(matchDate);
        match.setStartTime(startTime);
        match.setEndTime(endTime);
        match.setPlannedDurationMinutes(durationMinutes);
        match.setOfficialMatch(isOfficialMatch);
        match.setStatus(MatchStatus.BOOKED);
        match.setParticipants(participants != null ? participants : Collections.emptyList());

        BigDecimal venueFee = calculateVenueFee(court, durationMinutes);
        match.setVenueFee(venueFee);
        match.setTotalAmount(venueFee);

        if (isOfficialMatch) {
            match.setStatus(MatchStatus.PENDING_CONFIRM);
            try {
                String refereeId = refereeService.assignRefereeForMatch(
                        sportType, matchDate, startTime, endTime, match.getId());
                if (refereeId != null) {
                    match.setRefereeId(refereeId);
                    match.setStatus(MatchStatus.CONFIRMED);
                    logger.info("已分配裁判 {} 给赛事 {}", refereeId, match.getId());
                }
            } catch (Exception e) {
                logger.warn("分配裁判失败: {}", e.getMessage());
                match.setStatus(MatchStatus.PENDING_CONFIRM);
            }
        }

        match = matchRepository.save(match);
        logger.info("赛事创建成功: ID={}, 状态={}", match.getId(), match.getStatus());
        return match;
    }

    private void validateTimeAvailability(String courtId, LocalDate matchDate, 
                                          LocalTime startTime, LocalTime endTime, String excludeMatchId) {
        List<Match> conflicts = matchRepository.findConflictingMatches(courtId, matchDate, startTime, endTime, excludeMatchId);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("该时间段场地已被预约，冲突赛事数量: " + conflicts.size());
        }
    }

    private void validateCourtHours(Court court, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        Court.TimeSlot timeSlot = court.getAvailableHours().get(dayOfWeek);
        if (timeSlot == null) {
            throw new RuntimeException("该场地在" + dayOfWeek + "没有可用时间");
        }
        if (startTime.isBefore(timeSlot.getStartTime()) || endTime.isAfter(timeSlot.getEndTime())) {
            throw new RuntimeException("预约时间超出场地开放时间（" + 
                    timeSlot.getStartTime() + " - " + timeSlot.getEndTime() + "）");
        }
    }

    private BigDecimal calculateVenueFee(Court court, int durationMinutes) {
        BigDecimal hourlyRate = court.getHourlyRate();
        BigDecimal hours = BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return hourlyRate.multiply(hours);
    }

    public Optional<Match> getMatchById(String id) {
        return matchRepository.findById(id);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getMatchesByStatus(MatchStatus status) {
        return matchRepository.findByStatus(status);
    }

    public List<Match> getMatchesByDate(LocalDate date) {
        return matchRepository.findByMatchDate(date);
    }

    public Match updateMatchStatus(String matchId, MatchStatus newStatus) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();
        match.setStatus(newStatus);
        match.setUpdatedAt(LocalDateTime.now());
        return matchRepository.save(match);
    }

    public Match startMatch(String matchId) {
        return updateMatchStatus(matchId, MatchStatus.IN_PROGRESS);
    }

    public Match completeMatch(String matchId, LocalTime actualEndTime) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();
        
        if (actualEndTime == null) {
            actualEndTime = LocalTime.now();
        }
        
        match.setActualEndTime(actualEndTime);
        match.setStatus(MatchStatus.COMPLETED);
        match.setUpdatedAt(LocalDateTime.now());

        if (actualEndTime.isAfter(match.getEndTime())) {
            long overtimeMinutes = Duration.between(match.getEndTime(), actualEndTime).toMinutes();
            if (overtimeMinutes > 0) {
                Optional<Court> courtOpt = courtRepository.findById(match.getCourtId());
                if (courtOpt.isPresent()) {
                    Court court = courtOpt.get();
                    BigDecimal overtimeRate = court.getOvertimeRate() != null 
                            ? court.getOvertimeRate() 
                            : court.getHourlyRate();
                    BigDecimal overtimeHours = BigDecimal.valueOf(overtimeMinutes)
                            .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal overtimeFee = overtimeRate.multiply(overtimeHours);
                    match.setOvertimeFee(overtimeFee);
                    match.setTotalAmount(match.getTotalAmount().add(overtimeFee));
                    logger.info("赛事超时 {} 分钟，超时费用: {}", overtimeMinutes, overtimeFee);
                }
            }
        }

        return matchRepository.save(match);
    }

    public List<Match> getMatchesByMonth(int year, int month) {
        return matchRepository.findByMonth(year, month);
    }

    public List<Match> getMatchesByCourtId(String courtId) {
        return matchRepository.findByCourtId(courtId);
    }

    public List<Match> getMatchesByRefereeId(String refereeId) {
        return matchRepository.findByRefereeId(refereeId);
    }

    public Match confirmMatch(String matchId) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();
        if (match.getStatus() == MatchStatus.CONFIRMED || 
            match.getStatus() == MatchStatus.IN_PROGRESS ||
            match.getStatus() == MatchStatus.COMPLETED ||
            match.getStatus() == MatchStatus.CANCELLED) {
            throw new RuntimeException("赛事状态不允许确认");
        }
        match.setStatus(MatchStatus.CONFIRMED);
        match.setUpdatedAt(LocalDateTime.now());
        return matchRepository.save(match);
    }

    public Match reassignReferee(String matchId) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();
        if (match.getStatus() == MatchStatus.CANCELLED || 
            match.getStatus() == MatchStatus.COMPLETED) {
            throw new RuntimeException("赛事已取消或已完成");
        }
        try {
            String refereeId = refereeService.assignRefereeForMatch(
                    match.getSportType(), match.getMatchDate(), 
                    match.getStartTime(), match.getEndTime(), match.getId());
            if (refereeId != null) {
                match.setRefereeId(refereeId);
                match.setStatus(MatchStatus.CONFIRMED);
                match.setUpdatedAt(LocalDateTime.now());
                logger.info("赛事 {} 重新分配裁判成功: {}", match.getId(), refereeId);
                return matchRepository.save(match);
            }
        } catch (Exception e) {
            logger.warn("赛事 {} 重新分配裁判失败: {}", match.getId(), e.getMessage());
            match.setStatus(MatchStatus.PENDING_CONFIRM);
            match.setUpdatedAt(LocalDateTime.now());
            return matchRepository.save(match);
        }
        return match;
    }
}