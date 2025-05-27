package com.example.workreportplus.service;

import com.example.workreportplus.dto.OperationReportDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.workreportplus.service.GoogleSheetsService.OPERATIVE_TABLE_ID;

/**
 * @author Alex Sereda
 * @date 25.05.2025 16:39
 */

@Service
public class OperativeReportService {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;


    public OperativeReportService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }

    public List<OperationReportDto> getOperationReportsByDate(LocalDate targetDate, String regionName) throws IOException {
        String range = "ЗвітГруп!A2:L"; // A = Дата Звіту, L = ІД Населеного пункту
        List<List<Object>> rows = googleSheetsService.readSheet(OPERATIVE_TABLE_ID, range);

        List<OperationReportDto> result = new ArrayList<>();

        for (List<Object> row : rows) {
            if (row.size() < 12) continue; // Ensure all expected columns exist

            LocalDate rowDate = parseDate(row.get(0));
            if (!targetDate.equals(rowDate)) continue;

            OperationReportDto dto = new OperationReportDto();
            dto.setDate(rowDate);
            dto.setGroupName(getString(row, 1));
            dto.setRegionName(getString(row, 2));
            dto.setPlaceName(getString(row, 3));
            dto.setDescription(getString(row, 4));
            dto.setAmmo(getString(row, 5));
            dto.setAssets(getString(row, 6));
            dto.setResult(getString(row, 7));
            dto.setIsAmmoVerified(parseBoolean(row.get(9)));
            dto.setGroupId(parseUUID(row.get(10)));
            dto.setPlaceId(parseUUID(row.get(11)));

            result.add(dto);
        }

        return result;
    }

    private String getString(List<Object> row, int index) {
        return index < row.size() ? String.valueOf(row.get(index)).trim() : "";
    }

    private LocalDate parseDate(Object value) {
        try {
            if (value instanceof LocalDate) return (LocalDate) value;
            String str = String.valueOf(value).trim();
            return LocalDate.parse(str, DateTimeFormatter.ofPattern("dd.MM.yyyy")); // adjust if needed
        } catch (Exception e) {
            return null;
        }
    }

    private UUID parseUUID(Object value) {
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value == null) return false;
        String val = value.toString().trim().toLowerCase();
        return val.equals("true") || val.equals("так") || val.equals("yes") || val.equals("1");
    }


}
