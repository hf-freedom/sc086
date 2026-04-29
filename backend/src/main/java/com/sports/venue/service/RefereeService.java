package com.sports.venue.service;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.enums.SportType;
import com.sports.venue.model.Court;
import com.sports.venue.model.Match;
import com.sports.venue.model.Referee;
import com.sports.venue.repository.MatchRepository;
import com.sports.venue.repository.RefereeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RefereeService {

    private static final Logger logger = LoggerFactory.getLogger(RefereeService.class);

    @Autowired
    private RefereeRepository refereeRepository;

    @Autowired
    private MatchRepository matchRepository;

    public Referee createReferee(String name, String phone, String idCard,
                                  List<SportType> qualifiedSports, String qualificationLevel,
                                  BigDecimal hourlyRate) {
        Referee referee = new Referee(name, phone, qualificationLevel);
        referee.setIdCard(idCard);
        referee.setQualifiedSports(qualifiedSports != null ? qualifiedSports : new ArrayList<>());
        referee.setHourlyRate(hourlyRate);
        
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Referee.TimeSlot> slots = new ArrayList<>();
            slots.add(new Referee.TimeSlot(LocalTime.of(8, 0), LocalTime.of(12, 0)));
            slots.add(new Referee.TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0)));
            referee.getAvailableHours().put(day, slots);
        }
        
        return refereeRepository.save(referee);
    }

    public Optional<Referee> getRefereeById(String id) {
        return refereeRepository.findById(id);
    }

    public List<Referee> getAllReferees() {
        return refereeRepository.findAll();
    }

    public List<Referee> getActiveReferees() {
        return refereeRepository.findActiveReferees();
    }

    public List<Referee> getRefereesBySportType(SportType sportType) {
        return refereeRepository.findByQualifiedSport(sportType);
    }

    public Referee updateReferee(String id, String name, String phone,
                                  List<SportType> qualifiedSports, String qualificationLevel,
                                  BigDecimal hourlyRate) {
        Optional<Referee> refereeOpt = refereeRepository.findById(id);
        if (!refereeOpt.isPresent()) {
            throw new RuntimeException("裁判不存在");
        }
        Referee referee = refereeOpt.get();
        if (name != null) referee.setName(name);
        if (phone != null) referee.setPhone(phone);
        if (qualifiedSports != null) referee.setQualifiedSports(qualifiedSports);
        if (qualificationLevel != null) referee.setQualificationLevel(qualificationLevel);
        if (hourlyRate != null) referee.setHourlyRate(hourlyRate);
        return refereeRepository.save(referee);
    }

    public void setRefereeActive(String id, boolean active) {
        Optional<Referee> refereeOpt = refereeRepository.findById(id);
        if (!refereeOpt.isPresent()) {
            throw new RuntimeException("裁判不存在");
        }
        Referee referee = refereeOpt.get();
        referee.setActive(active);
        refereeRepository.save(referee);
    }

    public String assignRefereeForMatch(SportType sportType, LocalDate matchDate,
                                         LocalTime startTime, LocalTime endTime, String matchId) {
        logger.info("为赛事分配裁判: 运动类型={}, 日期={}, 时间={}-{}", 
                sportType, matchDate, startTime, endTime);

        List<Referee> qualifiedReferees = refereeRepository.findByQualifiedSport(sportType);
        if (qualifiedReferees.isEmpty()) {
            throw new RuntimeException("没有具备" + sportType.getDescription() + "项目资质的裁判");
        }

        for (Referee referee : qualifiedReferees) {
            if (isRefereeAvailable(referee, matchDate, startTime, endTime, matchId)) {
                logger.info("已分配裁判: {}", referee.getName());
                return referee.getId();
            }
        }

        throw new RuntimeException("没有可用的裁判在该时间段");
    }

    public boolean isRefereeAvailable(Referee referee, LocalDate matchDate,
                                       LocalTime startTime, LocalTime endTime, String excludeMatchId) {
        if (!referee.isActive()) {
            return false;
        }

        if (isRefereeOnLeave(referee, matchDate, startTime, endTime)) {
            return false;
        }

        if (!isWithinAvailableHours(referee, matchDate.getDayOfWeek(), startTime, endTime)) {
            return false;
        }

        return !hasConflictMatch(referee.getId(), matchDate, startTime, endTime, excludeMatchId);
    }

    private boolean isRefereeOnLeave(Referee referee, LocalDate matchDate,
                                      LocalTime startTime, LocalTime endTime) {
        for (Referee.LeaveRecord leave : referee.getLeaveRecords()) {
            if (!leave.isApproved()) {
                continue;
            }
            if (matchDate.equals(leave.getLeaveDate())) {
                if (hasTimeOverlap(startTime, endTime, leave.getStartTime(), leave.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWithinAvailableHours(Referee referee, DayOfWeek dayOfWeek,
                                            LocalTime startTime, LocalTime endTime) {
        List<Referee.TimeSlot> slots = referee.getAvailableHours().get(dayOfWeek);
        if (slots == null || slots.isEmpty()) {
            return false;
        }
        for (Referee.TimeSlot slot : slots) {
            if (!startTime.isBefore(slot.getStartTime()) && !endTime.isAfter(slot.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConflictMatch(String refereeId, LocalDate matchDate,
                                      LocalTime startTime, LocalTime endTime, String excludeMatchId) {
        List<Match> matches = matchRepository.findByRefereeId(refereeId);
        for (Match match : matches) {
            if (excludeMatchId != null && excludeMatchId.equals(match.getId())) {
                continue;
            }
            if (match.getStatus() == MatchStatus.CANCELLED) {
                continue;
            }
            if (matchDate.equals(match.getMatchDate())) {
                if (hasTimeOverlap(startTime, endTime, match.getStartTime(), match.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1,
                                    LocalTime start2, LocalTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    public Referee.LeaveRecord submitLeaveRequest(String refereeId, LocalDate leaveDate,
                                                    LocalTime startTime, LocalTime endTime, String reason) {
        Optional<Referee> refereeOpt = refereeRepository.findById(refereeId);
        if (!refereeOpt.isPresent()) {
            throw new RuntimeException("裁判不存在");
        }
        Referee referee = refereeOpt.get();
        
        Referee.LeaveRecord leaveRecord = new Referee.LeaveRecord();
        leaveRecord.setLeaveDate(leaveDate);
        leaveRecord.setStartTime(startTime);
        leaveRecord.setEndTime(endTime);
        leaveRecord.setReason(reason);
        
        referee.getLeaveRecords().add(leaveRecord);
        refereeRepository.save(referee);
        
        logger.info("裁判 {} 提交请假申请: {} {} - {}", referee.getName(), leaveDate, startTime, endTime);
        return leaveRecord;
    }

    public void approveLeaveRequest(String refereeId, int leaveIndex) {
        Optional<Referee> refereeOpt = refereeRepository.findById(refereeId);
        if (!refereeOpt.isPresent()) {
            throw new RuntimeException("裁判不存在");
        }
        Referee referee = refereeOpt.get();
        
        if (leaveIndex < 0 || leaveIndex >= referee.getLeaveRecords().size()) {
            throw new RuntimeException("请假记录不存在");
        }
        
        Referee.LeaveRecord leaveRecord = referee.getLeaveRecords().get(leaveIndex);
        leaveRecord.setApproved(true);
        refereeRepository.save(referee);
        
        reassignMatchesForLeave(referee.getId(), leaveRecord.getLeaveDate(),
                leaveRecord.getStartTime(), leaveRecord.getEndTime());
        
        logger.info("已批准裁判 {} 的请假申请", referee.getName());
    }

    private void reassignMatchesForLeave(String refereeId, LocalDate leaveDate,
                                          LocalTime startTime, LocalTime endTime) {
        List<Match> matches = matchRepository.findByRefereeId(refereeId);
        for (Match match : matches) {
            if (match.getStatus() == MatchStatus.CANCELLED || 
                match.getStatus() == MatchStatus.COMPLETED) {
                continue;
            }
            if (leaveDate.equals(match.getMatchDate())) {
                if (hasTimeOverlap(startTime, endTime, match.getStartTime(), match.getEndTime())) {
                    try {
                        String newRefereeId = assignRefereeForMatch(
                                match.getSportType(), match.getMatchDate(),
                                match.getStartTime(), match.getEndTime(), match.getId());
                        match.setRefereeId(newRefereeId);
                        matchRepository.save(match);
                        logger.info("赛事 {} 已重新分配裁判: {}", match.getId(), newRefereeId);
                    } catch (Exception e) {
                        logger.warn("无法为赛事 {} 重新分配裁判: {}", match.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    public List<Referee.TimeSlot> getRefereeAvailableSlots(String refereeId, DayOfWeek dayOfWeek) {
        Optional<Referee> refereeOpt = refereeRepository.findById(refereeId);
        if (!refereeOpt.isPresent()) {
            throw new RuntimeException("裁判不存在");
        }
        Referee referee = refereeOpt.get();
        return referee.getAvailableHours().getOrDefault(dayOfWeek, new ArrayList<>());
    }
}