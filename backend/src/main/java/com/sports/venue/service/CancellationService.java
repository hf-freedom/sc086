package com.sports.venue.service;

import com.sports.venue.enums.CancellationReason;
import com.sports.venue.enums.MatchStatus;
import com.sports.venue.model.FeeRecord;
import com.sports.venue.model.Match;
import com.sports.venue.repository.FeeRecordRepository;
import com.sports.venue.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CancellationService {

    private static final Logger logger = LoggerFactory.getLogger(CancellationService.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    public Match cancelMatch(String matchId, CancellationReason reason, String note) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();

        if (match.getStatus() == MatchStatus.CANCELLED || 
            match.getStatus() == MatchStatus.COMPLETED) {
            throw new RuntimeException("赛事已经完成或已取消");
        }

        BigDecimal refundAmount = calculateRefundAmount(match, reason);

        match.setStatus(MatchStatus.CANCELLED);
        match.setCancellationReason(reason.getDescription());
        match.setCancellationNote(note);
        match.setRefundAmount(refundAmount);
        match.setCancelledAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        match = matchRepository.save(match);

        createFeeRecords(match, reason);

        logger.info("赛事 {} 已取消，原因: {}, 退款金额: {}", matchId, reason, refundAmount);
        return match;
    }

    public Match cancelMatchDueToWeather(String matchId, String weatherDescription) {
        return cancelMatch(matchId, CancellationReason.WEATHER, "天气异常: " + weatherDescription);
    }

    public Match cancelMatchDueToTeam(String matchId, String note) {
        return cancelMatch(matchId, CancellationReason.TEAM_CANCEL, note);
    }

    public Match cancelMatchDueToVenueMaintenance(String matchId, String note) {
        return cancelMatch(matchId, CancellationReason.VENUE_MAINTENANCE, note);
    }

    private BigDecimal calculateRefundAmount(Match match, CancellationReason reason) {
        BigDecimal totalAmount = match.getTotalAmount();
        
        switch (reason) {
            case WEATHER:
            case VENUE_MAINTENANCE:
            case REFEREE_LEAVE:
                return totalAmount;
            case TEAM_CANCEL:
                long hoursBeforeMatch = java.time.Duration.between(
                        LocalDateTime.now(),
                        java.time.LocalDateTime.of(match.getMatchDate(), match.getStartTime())
                ).toHours();
                
                if (hoursBeforeMatch >= 48) {
                    return totalAmount;
                } else if (hoursBeforeMatch >= 24) {
                    return totalAmount.multiply(new BigDecimal("0.8"));
                } else if (hoursBeforeMatch >= 12) {
                    return totalAmount.multiply(new BigDecimal("0.5"));
                } else {
                    return BigDecimal.ZERO;
                }
            case OTHER:
                return totalAmount.multiply(new BigDecimal("0.5"));
            default:
                return BigDecimal.ZERO;
        }
    }

    private void createFeeRecords(Match match, CancellationReason reason) {
        if (match.getVenueFee() != null && match.getVenueFee().compareTo(BigDecimal.ZERO) > 0) {
            FeeRecord venueFee = new FeeRecord();
            venueFee.setMatchId(match.getId());
            venueFee.setCourtId(match.getCourtId());
            venueFee.setFeeType(FeeRecord.FeeType.VENUE_RENT);
            venueFee.setAmount(match.getVenueFee());
            venueFee.setFeeDate(LocalDate.now());
            venueFee.setDescription("场地租金 - 取消");
            feeRecordRepository.save(venueFee);
        }

        if (match.getRefereeFee() != null && match.getRefereeFee().compareTo(BigDecimal.ZERO) > 0) {
            FeeRecord refereeFee = new FeeRecord();
            refereeFee.setMatchId(match.getId());
            refereeFee.setRefereeId(match.getRefereeId());
            refereeFee.setFeeType(FeeRecord.FeeType.REFEREE_FEE);
            refereeFee.setAmount(match.getRefereeFee());
            refereeFee.setFeeDate(LocalDate.now());
            refereeFee.setDescription("裁判费用 - 取消");
            feeRecordRepository.save(refereeFee);
        }

        if (match.getRefundAmount() != null && match.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            FeeRecord refundFee = new FeeRecord();
            refundFee.setMatchId(match.getId());
            refundFee.setFeeType(FeeRecord.FeeType.REFUND);
            refundFee.setAmount(match.getRefundAmount().negate());
            refundFee.setFeeDate(LocalDate.now());
            refundFee.setDescription("退款 - 原因: " + reason.getDescription());
            feeRecordRepository.save(refundFee);
        }
    }

    public Match rescheduleMatch(String matchId, LocalDate newDate, 
                                   java.time.LocalTime newStartTime, int newDurationMinutes) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new RuntimeException("赛事已完成，无法改期");
        }

        java.time.LocalTime newEndTime = newStartTime.plusMinutes(newDurationMinutes);

        List<Match> conflicts = matchRepository.findConflictingMatches(
                match.getCourtId(), newDate, newStartTime, newEndTime, matchId);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("新时间段场地已被预约");
        }

        match.setMatchDate(newDate);
        match.setStartTime(newStartTime);
        match.setEndTime(newEndTime);
        match.setPlannedDurationMinutes(newDurationMinutes);
        match.setUpdatedAt(LocalDateTime.now());

        return matchRepository.save(match);
    }

    public boolean isEligibleForFullRefund(CancellationReason reason) {
        return reason == CancellationReason.WEATHER || 
               reason == CancellationReason.VENUE_MAINTENANCE ||
               reason == CancellationReason.REFEREE_LEAVE;
    }
}