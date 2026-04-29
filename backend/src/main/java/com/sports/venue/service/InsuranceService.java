package com.sports.venue.service;

import com.sports.venue.model.Insurance;
import com.sports.venue.model.Match;
import com.sports.venue.repository.InsuranceRepository;
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
import java.util.UUID;

@Service
public class InsuranceService {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceService.class);

    private static final BigDecimal STANDARD_PREMIUM = new BigDecimal("50.00");
    private static final BigDecimal STANDARD_COVERAGE = new BigDecimal("100000.00");

    @Autowired
    private InsuranceRepository insuranceRepository;

    @Autowired
    private MatchRepository matchRepository;

    public Insurance purchaseInsurance(String matchId, String participantName,
                                        String participantIdCard, String participantPhone) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }
        Match match = matchOpt.get();

        LocalDate startDate = match.getMatchDate();
        LocalDate endDate = startDate.plusDays(1);

        List<Insurance> existing = insuranceRepository.findByParticipantIdCard(participantIdCard);
        for (Insurance ins : existing) {
            if (ins.isActive() && !startDate.isAfter(ins.getEndDate()) && !endDate.isBefore(ins.getStartDate())) {
                logger.info("参与者 {} 已有有效保险", participantName);
                return ins;
            }
        }

        Insurance insurance = new Insurance();
        insurance.setMatchId(matchId);
        insurance.setParticipantName(participantName);
        insurance.setParticipantIdCard(participantIdCard);
        insurance.setParticipantPhone(participantPhone);
        insurance.setPremium(STANDARD_PREMIUM);
        insurance.setCoverageAmount(STANDARD_COVERAGE);
        insurance.setStartDate(startDate);
        insurance.setEndDate(endDate);
        insurance.setInsuranceType("体育赛事意外险");
        insurance.setPolicyNumber("INS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        insurance.setPurchasedAt(LocalDateTime.now());
        insurance.setActive(true);

        insurance = insuranceRepository.save(insurance);
        
        match.getInsurances().add(insurance);
        match.setInsuranceFee(match.getInsuranceFee().add(STANDARD_PREMIUM));
        match.setTotalAmount(match.getTotalAmount().add(STANDARD_PREMIUM));
        matchRepository.save(match);

        logger.info("已为参与者 {} 购买保险，保单号: {}", participantName, insurance.getPolicyNumber());
        return insurance;
    }

    public List<Insurance> purchaseInsurancesForMatch(String matchId, List<Match.Participant> participants) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new RuntimeException("赛事不存在");
        }

        for (Match.Participant participant : participants) {
            purchaseInsurance(matchId, participant.getName(), participant.getIdCard(), participant.getPhone());
        }

        return insuranceRepository.findByMatchId(matchId);
    }

    public Optional<Insurance> getInsuranceById(String id) {
        return insuranceRepository.findById(id);
    }

    public List<Insurance> getInsurancesByMatch(String matchId) {
        return insuranceRepository.findByMatchId(matchId);
    }

    public List<Insurance> getInsurancesByParticipant(String idCard) {
        return insuranceRepository.findByParticipantIdCard(idCard);
    }

    public void cancelInsurance(String id) {
        Optional<Insurance> insuranceOpt = insuranceRepository.findById(id);
        if (!insuranceOpt.isPresent()) {
            throw new RuntimeException("保险不存在");
        }
        Insurance insurance = insuranceOpt.get();
        insurance.setActive(false);
        insuranceRepository.save(insurance);
        logger.info("已取消保险: {}", insurance.getPolicyNumber());
    }

    public BigDecimal getStandardPremium() {
        return STANDARD_PREMIUM;
    }

    public BigDecimal getStandardCoverage() {
        return STANDARD_COVERAGE;
    }
}