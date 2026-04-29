package com.sports.venue.controller;

import com.sports.venue.model.Insurance;
import com.sports.venue.model.Match;
import com.sports.venue.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/insurance")
@CrossOrigin(origins = "*")
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;

    @GetMapping("/info")
    public Map<String, BigDecimal> getInsuranceInfo() {
        Map<String, BigDecimal> info = new HashMap<>();
        info.put("standardPremium", insuranceService.getStandardPremium());
        info.put("standardCoverage", insuranceService.getStandardCoverage());
        return info;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insurance> getInsuranceById(@PathVariable String id) {
        Optional<Insurance> insurance = insuranceService.getInsuranceById(id);
        return insurance.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/match/{matchId}")
    public List<Insurance> getInsurancesByMatch(@PathVariable String matchId) {
        return insuranceService.getInsurancesByMatch(matchId);
    }

    @GetMapping("/participant/{idCard}")
    public List<Insurance> getInsurancesByParticipant(@PathVariable String idCard) {
        return insuranceService.getInsurancesByParticipant(idCard);
    }

    @PostMapping("/purchase")
    public Insurance purchaseInsurance(@RequestParam String matchId,
                                        @RequestParam String participantName,
                                        @RequestParam String participantIdCard,
                                        @RequestParam String participantPhone) {
        return insuranceService.purchaseInsurance(matchId, participantName, participantIdCard, participantPhone);
    }

    @PostMapping("/purchase/batch")
    public List<Insurance> purchaseInsurancesForMatch(@RequestParam String matchId,
                                                        @RequestBody List<Match.Participant> participants) {
        return insuranceService.purchaseInsurancesForMatch(matchId, participants);
    }

    @PutMapping("/{id}/cancel")
    public void cancelInsurance(@PathVariable String id) {
        insuranceService.cancelInsurance(id);
    }
}