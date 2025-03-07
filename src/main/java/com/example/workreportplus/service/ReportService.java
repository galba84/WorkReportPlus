package com.example.workreportplus.service;

import com.example.workreportplus.request.searchparams.SearchParams;
import com.example.workreportplus.response.ReportResponse;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<ReportResponse> getReports (SearchParams searchParams);
    ReportResponse getReportById(UUID id);
}
