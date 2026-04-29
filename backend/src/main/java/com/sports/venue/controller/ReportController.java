package com.sports.venue.controller;

import com.sports.venue.model.MonthlyReport;
import com.sports.venue.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<MonthlyReport> getAllReports(@RequestParam(required = false) Integer year) {
        if (year != null) {
            return reportService.getReportsByYear(year);
        }
        return reportService.getAllReports();
    }

    @GetMapping("/monthly")
    public MonthlyReport getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return reportService.getReportForMonth(year, month);
    }

    @PostMapping("/generate")
    public MonthlyReport generateReport(@RequestParam int year, @RequestParam int month) {
        return reportService.generateReportForMonth(year, month);
    }
}