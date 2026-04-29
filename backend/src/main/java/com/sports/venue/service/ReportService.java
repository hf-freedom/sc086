package com.sports.venue.service;

import com.sports.venue.enums.MatchStatus;
import com.sports.venue.model.Court;
import com.sports.venue.model.FeeRecord;
import com.sports.venue.model.Match;
import com.sports.venue.model.MonthlyReport;
import com.sports.venue.repository.CourtRepository;
import com.sports.venue.repository.FeeRecordRepository;
import com.sports.venue.repository.MatchRepository;
import com.sports.venue.repository.MonthlyReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private FeeRecordRepository feeRecordRepository;

    @Autowired
    private MonthlyReportRepository monthlyReportRepository;

    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyReport() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        generateReportForMonth(lastMonth.getYear(), lastMonth.getMonthValue());
    }

    public MonthlyReport generateReportForMonth(int year, int month) {
        YearMonth reportMonth = YearMonth.of(year, month);
        
        Optional<MonthlyReport> existing = monthlyReportRepository.findByMonth(reportMonth);
        if (existing.isPresent()) {
            logger.info("月度报表已存在: {}-{}", year, month);
            return existing.get();
        }

        MonthlyReport report = new MonthlyReport();
        report.setReportMonth(reportMonth);
        report.setGeneratedAt(LocalDate.now());

        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        List<Match> matches = matchRepository.findByMatchDateBetween(startDate, endDate);

        int totalMatches = matches.size();
        report.setTotalMatches(totalMatches);

        long completedCount = matches.stream()
                .filter(m -> MatchStatus.COMPLETED.equals(m.getStatus()))
                .count();
        report.setCompletedMatches((int) completedCount);

        long cancelledCount = matches.stream()
                .filter(m -> MatchStatus.CANCELLED.equals(m.getStatus()))
                .count();
        report.setCancelledMatches((int) cancelledCount);

        if (totalMatches > 0) {
            double cancellationRate = (double) cancelledCount / totalMatches * 100;
            report.setCancellationRate(Math.round(cancellationRate * 100.0) / 100.0);
        } else {
            report.setCancellationRate(0.0);
        }

        List<Court> courts = courtRepository.findAll();
        report.setTotalCourts(courts.size());

        int totalAvailableHours = calculateTotalAvailableHours(courts, reportMonth);
        report.setTotalAvailableHours(totalAvailableHours);

        int actualUsedHours = calculateActualUsedHours(matches);
        report.setActualUsedHours(actualUsedHours);

        if (totalAvailableHours > 0) {
            double utilizationRate = (double) actualUsedHours / totalAvailableHours * 100;
            report.setCourtUtilizationRate(Math.round(utilizationRate * 100.0) / 100.0);
        } else {
            report.setCourtUtilizationRate(0.0);
        }

        List<FeeRecord> feeRecords = feeRecordRepository.findByMonth(reportMonth);

        BigDecimal totalVenueRevenue = feeRecords.stream()
                .filter(r -> FeeRecord.FeeType.VENUE_RENT.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalVenueRevenue(totalVenueRevenue);

        BigDecimal totalRefereeFee = feeRecords.stream()
                .filter(r -> FeeRecord.FeeType.REFEREE_FEE.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRefereeFee(totalRefereeFee);

        BigDecimal totalOvertimeFee = feeRecords.stream()
                .filter(r -> FeeRecord.FeeType.OVERTIME_FEE.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalOvertimeFee(totalOvertimeFee);

        BigDecimal totalInsuranceFee = feeRecords.stream()
                .filter(r -> FeeRecord.FeeType.INSURANCE_FEE.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalInsuranceFee(totalInsuranceFee);

        BigDecimal totalRevenue = totalVenueRevenue.add(totalRefereeFee)
                .add(totalOvertimeFee).add(totalInsuranceFee);
        report.setTotalRevenue(totalRevenue);

        BigDecimal totalRefund = feeRecords.stream()
                .filter(r -> FeeRecord.FeeType.REFUND.equals(r.getFeeType()))
                .map(FeeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRefund(totalRefund);

        report.setNetRevenue(totalRevenue.add(totalRefund));

        report = monthlyReportRepository.save(report);

        logger.info("月度报表生成完成: {}-{}, 总收入: {}, 取消率: {}%, 利用率: {}%",
                year, month, report.getNetRevenue(),
                report.getCancellationRate(), report.getCourtUtilizationRate());

        return report;
    }

    private int calculateTotalAvailableHours(List<Court> courts, YearMonth month) {
        int totalHours = 0;
        int daysInMonth = month.lengthOfMonth();

        for (Court court : courts) {
            if (!court.isActive()) continue;

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = month.atDay(day);
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                Court.TimeSlot slot = court.getAvailableHours().get(dayOfWeek);
                if (slot != null) {
                    Duration duration = Duration.between(slot.getStartTime(), slot.getEndTime());
                    totalHours += duration.toHours();
                }
            }
        }
        return totalHours;
    }

    private int calculateActualUsedHours(List<Match> matches) {
        int totalMinutes = 0;
        for (Match match : matches) {
            if (MatchStatus.COMPLETED.equals(match.getStatus()) || 
                MatchStatus.IN_PROGRESS.equals(match.getStatus())) {
                totalMinutes += match.getPlannedDurationMinutes();
                if (match.getActualEndTime() != null && match.getEndTime() != null) {
                    Duration overtime = Duration.between(match.getEndTime(), match.getActualEndTime());
                    if (!overtime.isNegative()) {
                        totalMinutes += overtime.toMinutes();
                    }
                }
            }
        }
        return totalMinutes / 60;
    }

    public MonthlyReport getReportForMonth(int year, int month) {
        YearMonth reportMonth = YearMonth.of(year, month);
        Optional<MonthlyReport> report = monthlyReportRepository.findByMonth(reportMonth);
        if (report.isPresent()) {
            return report.get();
        }
        return generateReportForMonth(year, month);
    }

    public List<MonthlyReport> getAllReports() {
        return monthlyReportRepository.findAll();
    }

    public List<MonthlyReport> getReportsByYear(int year) {
        return monthlyReportRepository.findByYear(year);
    }
}