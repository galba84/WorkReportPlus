package com.example.workreportplus.service;

import com.example.workreportplus.request.searchparams.SearchParams;
import com.example.workreportplus.response.ReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<ReportResponse> getReports (SearchParams searchParams);
    ReportResponse getReportById(UUID id);

    List<UUID> getReportIdsByRegionAndPeriod(UUID regionId, LocalDate fromDate, LocalDate toDate);
}
