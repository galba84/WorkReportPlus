package com.example.workreportplus.service;


import com.example.workreportplus.ENUM.AreaType;
import com.example.workreportplus.ENUM.PersonnelLossesType;
import com.example.workreportplus.response.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportService {


    public static List<DailyRegionReportResponse> generateSampleReports() {
        List<DailyRegionReportResponse> reports = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            DailyRegionReportResponse report = new DailyRegionReportResponse();
            report.setId("RPT-" + i);
            report.setDate(LocalDate.now().minusDays(i));
            report.setRegion("Region " + i);
            report.setDescription("Sample Description for Report " + i);
            report.setCreatedBy("User " + i);
            report.setCreatedOn(LocalDate.now().minusDays(i));
            report.setUpdatedBy("Admin " + i);
            report.setUpdatedOn(LocalDate.now());
            report.setStatus("Active");

            // Generate 3 Group Reports
            List<DailyGroupReportResponse> groupReports = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                DailyGroupReportResponse groupReport = new DailyGroupReportResponse();
                groupReport.setId("GRP-" + i + "-" + j);
                groupReport.setRegionReportId(report.getId());
                groupReport.setRegion("Region " + i);
                groupReport.setDate(LocalDate.now().minusDays(i + j));
                groupReport.setCreatedBy("Group User " + j);
                groupReport.setStatus("Completed");
                groupReport.setDescription("Group Report Description " + j);

                groupReport.setWorkingAreas(generateWorkingAreas());
                groupReport.setContractors(generateContractors());
                groupReport.setContractorLooses(generateContractorLosses());

                groupReports.add(groupReport);
            }
            report.setGroupReports(groupReports);

            reports.add(report);
        }

        return reports;
    }

    private static List<WorkingAreaResponse> generateWorkingAreas() {
        return Arrays.asList(
                new WorkingAreaResponse("Area 1", AreaType.COUNTY, "County A", "District A", "Region A"),
                new WorkingAreaResponse("Area 2", AreaType.DISTRICT, "County B", "District B", "Region B"),
                new WorkingAreaResponse("Area 3", AreaType.REGION, "County C", "District C", "Region C")
        );
    }

    private static List<ContractorResponse> generateContractors() {
        return Arrays.asList(
                new ContractorResponse("nameq"),
                new ContractorResponse("name2"),
                new ContractorResponse("name3")
        );
    }

    private static List<ContractorLoosesResponse> generateContractorLosses() {
        return Arrays.asList(
                new ContractorLoosesResponse("nameq",PersonnelLossesType.LOSS_200),
                new ContractorLoosesResponse("nameq",PersonnelLossesType.LOSS_300),
                new ContractorLoosesResponse("nameq",PersonnelLossesType.LOSS_400)
        );
    }

}
