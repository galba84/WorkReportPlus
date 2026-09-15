package com.example.workreportplus.service;

import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.ContractorWorkReportDtoRecord;
import com.example.workreportplus.dto.Report100Record;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class Report100Service {
    GroupReportService groupReportService;
    RegionService regionService;
    RegionReportService regionReportService;
    ContractorService contractorService;

    public Report100Service(GroupReportService groupReportService, RegionService regionService,
                            RegionReportService regionReportService, ContractorService contractorService) {
        this.groupReportService = groupReportService;
        this.regionService = regionService;
        this.regionReportService = regionReportService;
        this.contractorService = contractorService;
    }

    public List<Report100Record> getReportForRegionAndMonth(String regionName,
                                                            @Valid LocalDate reportDate) {
        LocalDate fromDate = reportDate.withDayOfMonth(1);
        LocalDate toDate = reportDate.withDayOfMonth(reportDate.lengthOfMonth());
        UUID regionId = regionService.getRegionIdByName(regionName);

        List<UUID> reportIds = regionReportService.getReportIdsByRegionAndPeriod(regionId, fromDate, toDate);

        List<ContractorWorkReportDtoRecord> contractorWorkData = groupReportService
                .getContractorWorkDataByPeriodAndReportIds(fromDate, toDate, reportIds);

        List<UUID> contractorIds = contractorWorkData.stream()
                .map(ContractorWorkReportDtoRecord::contractorId)
                .distinct()
                .toList();

        List<ContractorDto> contractors = contractorService.getAllContractorByIds(contractorIds);

        Map<UUID, ContractorDto> contractorMap = contractors.stream()
                .collect(Collectors.toMap(ContractorDto::getId, Function.identity()));

        return contractorWorkData.stream()
                .map(e -> {
                    ContractorDto contractor = contractorMap.get(e.contractorId());
                    String fullName = contractor.getFirstName() + " " + contractor.getLastName();

                    return new Report100Record(
                            e.contractorId().toString(),
                            fullName,
                            contractor.getC_rank(),
                            contractor.getNickName(),
                            e.groupName(),
                            e.date(),
                            Boolean.TRUE.equals(e.isWorked()) ? 100 : 30 // coefficient based on isWorked
                    );
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<List<String>> buildAttendanceTable(List<Report100Record> records, int daysInMonth) {
        // Group records by contractor ID
        Map<String, List<Report100Record>> grouped = records.stream()
                .collect(Collectors.groupingBy(Report100Record::getContractorId));

        List<List<String>> table = new ArrayList<>();

        for (List<Report100Record> contractorRecords : grouped.values()) {
            // Use first record for static data
            Report100Record base = contractorRecords.get(0);

            List<String> row = new ArrayList<>();
            row.add(base.getName());
            row.add(base.getRank());
            row.add(base.getNickname());
            row.add(base.getGroupName());

            // Map: dayOfMonth -> coefficient
            Map<Integer, Integer> dayToCoef = contractorRecords.stream()
                    .collect(Collectors.toMap(
                            r -> r.getDate().getDayOfMonth(),
                            Report100Record::getCoefficient,
                            (a, b) -> a // handle duplicates
                    ));

            // Fill days: if missing, default to 0
            for (int day = 1; day <= daysInMonth; day++) {
                int coef = dayToCoef.getOrDefault(day, 0);
                row.add(String.valueOf(coef));
            }

            table.add(row);
        }

        return table;
    }



    public byte[] exportToExcel(String regionName, LocalDate reportDate) {
        // 1) fetch raw data and build the 2D table
        List<Report100Record> records = getReportForRegionAndMonth(regionName, reportDate);
        int daysInMonth = YearMonth.from(reportDate).lengthOfMonth();
        List<List<String>> table = buildAttendanceTable(records, daysInMonth);

        // 2) create workbook
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Attendance");

            // 3) header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Rank");
            header.createCell(2).setCellValue("Nickname");
            header.createCell(3).setCellValue("Group");
            for (int d = 1; d <= daysInMonth; d++) {
                header.createCell(3 + d).setCellValue("Day " + d);
            }

            // 4) data rows
            for (int i = 0; i < table.size(); i++) {
                List<String> rowData = table.get(i);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < rowData.size(); j++) {
                    row.createCell(j).setCellValue(rowData.get(j));
                }
            }

            // 5) auto-size columns (optional)
            for (int col = 0; col < 4 + daysInMonth; col++) {
                sheet.autoSizeColumn(col);
            }

            // 6) write out and return bytes
            wb.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel report", e);
        }
    }
}
