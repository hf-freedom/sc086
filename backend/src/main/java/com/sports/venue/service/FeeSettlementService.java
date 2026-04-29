package com.sports.venue.service;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.model.Court;
import com.sports.venue.model.FeeRecord;
import com.sports.venue.model.Match;
import com.sports.venue.model.Referee;
import com.sports.venue.repository.CourtRepository;
import com.sports.venue.repository.FeeRecordRepository;
import com.sports.venue.repository.MatchRepository;
import com.sports.venue.repository.RefereeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class FeeSettlementService {

    private static final Logger logger = LoggerFactory.getLogger(FeeSettlementService.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private RefereeRepository refereeRepository;

    public void settleMatchFees(String matchId) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();

        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new RuntimeException("赛事未完成，无法结算");
        }

        calculateAndRecordVenueFee(match);
        calculateAndRecordRefereeFee(match);
        calculateAndRecordOvertimeFee(match);

        match.setUpdatedAt(LocalDateTime.now());
        matchRepository.save(match);

        logger.info("赛事 {} 费用结算完成", matchId);
    }

    private void calculateAndRecordVenueFee(Match match) {
        Optional<Court> courtOpt = courtRepository.findById(match.getCourtId());
        if (!courtOpt.isPresent()) {
            logger.warn("场地不存在，无法计算场地费用");
            return;
        }
        Court court = courtOpt.get();

        BigDecimal venueFee = calculateVenueFee(court, match.getPlannedDurationMinutes());
        match.setVenueFee(venueFee);

        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setMatchId(match.getId());
        feeRecord.setCourtId(match.getCourtId());
        feeRecord.setFeeType(FeeRecord.FeeType.VENUE_RENT);
        feeRecord.setAmount(venueFee);
        feeRecord.setFeeDate(match.getMatchDate());
        feeRecord.setDescription("场地租金 - " + court.getName() + 
                " (" + match.getPlannedDurationMinutes() + "分钟)");
        feeRecordRepository.save(feeRecord);

        logger.info("场地费用已记录: {}", venueFee);
    }

    private void calculateAndRecordRefereeFee(Match match) {
        if (match.getRefereeId() == null) {
            logger.info("赛事没有分配裁判，跳过裁判费用计算");
            return;
        }

        Optional<Referee> refereeOpt = refereeRepository.findById(match.getRefereeId());
        if (!refereeOpt.isPresent()) {
            logger.warn("裁判不存在，无法计算裁判费用");
            return;
        }
        Referee referee = refereeOpt.get();

        BigDecimal refereeFee = calculateRefereeFee(referee, match.getPlannedDurationMinutes());
        match.setRefereeFee(refereeFee);

        FeeRecord feeRecord = new FeeRecord();
        feeRecord.setMatchId(match.getId());
        feeRecord.setRefereeId(match.getRefereeId());
        feeRecord.setFeeType(FeeRecord.FeeType.REFEREE_FEE);
        feeRecord.setAmount(refereeFee);
        feeRecord.setFeeDate(match.getMatchDate());
        feeRecord.setDescription("裁判费用 - " + referee.getName() + 
                " (" + match.getPlannedDurationMinutes() + "分钟)");
        feeRecordRepository.save(feeRecord);

        logger.info("裁判费用已记录: {}", refereeFee);
    }

    private void calculateAndRecordOvertimeFee(Match match) {
        if (match.getActualEndTime() == null) {
            return;
        }

        LocalTime startTime = match.getStartTime();
        LocalTime plannedEndTime = match.getEndTime();
        LocalTime actualEndTime = match.getActualEndTime();

        if (actualEndTime.isAfter(plannedEndTime)) {
            long overtimeMinutes = Duration.between(plannedEndTime, actualEndTime).toMinutes();
            if (overtimeMinutes > 0) {
                Optional<Court> courtOpt = courtRepository.findById(match.getCourtId());
                if (courtOpt.isPresent()) {
                    Court court = courtOpt.get();
                    BigDecimal overtimeRate = court.getOvertimeRate() != null 
                            ? court.getOvertimeRate() 
                            : court.getHourlyRate();
                    BigDecimal overtimeFee = calculateOvertimeFee(overtimeRate, overtimeMinutes);
                    match.setOvertimeFee(overtimeFee);

                    FeeRecord feeRecord = new FeeRecord();
                    feeRecord.setMatchId(match.getId());
                    feeRecord.setCourtId(match.getCourtId());
                    feeRecord.setFeeType(FeeRecord.FeeType.OVERTIME_FEE);
                    feeRecord.setAmount(overtimeFee);
                    feeRecord.setFeeDate(match.getMatchDate());
                    feeRecord.setDescription("超时费用 - " + overtimeMinutes + "分钟");
                    feeRecordRepository.save(feeRecord);

                    logger.info("超时费用已记录: 超时 {} 分钟, 费用 {}", overtimeMinutes, overtimeFee);
                }
            }
        }
    }

    private BigDecimal calculateVenueFee(Court court, int durationMinutes) {
        BigDecimal hourlyRate = court.getHourlyRate();
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) == 0) {
            hourlyRate = new BigDecimal("100.00");
        }
        BigDecimal hours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return hourlyRate.multiply(hours);
    }

    private BigDecimal calculateRefereeFee(Referee referee, int durationMinutes) {
        BigDecimal hourlyRate = referee.getHourlyRate();
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) == 0) {
            hourlyRate = new BigDecimal("150.00");
        }
        BigDecimal hours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return hourlyRate.multiply(hours);
    }

    private BigDecimal calculateOvertimeFee(BigDecimal overtimeRate, long overtimeMinutes) {
        BigDecimal hours = BigDecimal.valueOf(overtimeMinutes)
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return overtimeRate.multiply(hours);
    }

    public BigDecimal getTotalVenueRevenue(LocalDate startDate, LocalDate endDate) {
        return feeRecordRepository.findByMonth(java.time.YearMonth.from(startDate)).stream()
                .filter(r -> FeeRecord.FeeType.VENUE_RENT.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRefereeFee(LocalDate startDate, LocalDate endDate) {
        return feeRecordRepository.findByMonth(java.time.YearMonth.from(startDate)).stream()
                .filter(r -> FeeRecord.FeeType.REFEREE_FEE.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalOvertimeFee(LocalDate startDate, LocalDate endDate) {
        return feeRecordRepository.findByMonth(java.time.YearMonth.from(startDate)).stream()
                .filter(r -> FeeRecord.FeeType.OVERTIME_FEE.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRefund(LocalDate startDate, LocalDate endDate) {
        return feeRecordRepository.findByMonth(java.time.YearMonth.from(startDate)).stream()
                .filter(r -> FeeRecord.FeeType.REFUND.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}