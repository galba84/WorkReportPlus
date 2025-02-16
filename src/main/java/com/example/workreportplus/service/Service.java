package com.example.workreportplus.service;

import com.example.workreportplus.model.Report;
import com.example.workreportplus.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> searchReports(String startDate, String endDate, String region) {
        if (region == null || region.isEmpty()) {
            return reportRepository.findByDateRange(startDate, endDate);
        } else {
            return reportRepository.findByDateRangeAndRegion(startDate, endDate, region);
        }
    }
}
