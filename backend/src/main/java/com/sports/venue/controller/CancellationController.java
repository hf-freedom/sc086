package com.sports.venue.controller;

import com.sports.venue.enums.CancellationReason;
import com.sports.venue.model.Match;
import com.sports.venue.service.CancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/cancellation")
@CrossOrigin(origins = "*")
public class CancellationController {

    @Autowired
    private CancellationService cancellationService;

    @PostMapping("/{matchId}")
    public Match cancelMatch(@PathVariable String matchId,
                              @RequestParam CancellationReason reason,
                              @RequestParam(required = false) String note) {
        return cancellationService.cancelMatch(matchId, reason, note);
    }

    @PostMapping("/{matchId}/weather")
    public Match cancelMatchDueToWeather(@PathVariable String matchId,
                                          @RequestParam String weatherDescription) {
        return cancellationService.cancelMatchDueToWeather(matchId, weatherDescription);
    }

    @PostMapping("/{matchId}/team")
    public Match cancelMatchDueToTeam(@PathVariable String matchId,
                                        @RequestParam(required = false) String note) {
        return cancellationService.cancelMatchDueToTeam(matchId, note);
    }

    @PostMapping("/{matchId}/reschedule")
    public Match rescheduleMatch(@PathVariable String matchId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newStartTime,
                                  @RequestParam int newDurationMinutes) {
        return cancellationService.rescheduleMatch(matchId, newDate, newStartTime, newDurationMinutes);
    }

    @GetMapping("/refund-policy")
    public String getRefundPolicy() {
        return "退款政策：\n" +
                "1. 天气原因、场馆维护、裁判请假 - 全额退款\n" +
                "2. 团队取消：\n" +
                "   - 提前48小时 - 全额退款\n" +
                "   - 提前24-48小时 - 80%退款\n" +
                "   - 提前12-24小时 - 50%退款\n" +
                "   - 提前少于12小时 - 不退款\n" +
                "3. 其他原因 - 50%退款";
    }
}