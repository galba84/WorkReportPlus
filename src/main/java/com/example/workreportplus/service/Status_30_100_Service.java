package com.example.workreportplus.service;

import com.example.workreportplus.ENUM.ServiceStatus;
import com.example.workreportplus.Utils.DateUtils;
import com.example.workreportplus.dto.ContractorServiceStatusDto;
import com.google.api.services.sheets.v4.model.*;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static com.example.workreportplus.service.GoogleSheetsService.regionTo100_30_Map;

/**
 * @author Alex Sereda
 * @date 27.05.2025 22:40
 */

@Service
public class Status_30_100_Service {
    private final DSLContext dsl;
    private final GoogleSheetsService googleSheetsService;
    private final ContractorService contractorService;

    public Status_30_100_Service(DSLContext dsl, GoogleSheetsService googleSheetsService, ContractorService contractorService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
        this.contractorService = contractorService;
    }

    @Cacheable("getContractorServiceStatus")
    public List<ContractorServiceStatusDto> getContractorServiceStatus(LocalDate date, UUID regionId) throws IOException {
        String tableId = regionTo100_30_Map.getOrDefault(regionId.toString(), null);
        if (tableId == null) return List.of();

        String monthYear = DateUtils.formatUkrainianMonthYear(date);
        String range = monthYear + "!C9:AM";

        Spreadsheet spreadsheet = googleSheetsService.readSheetWithGrid(tableId, range);

        List<ContractorServiceStatusDto> results = new ArrayList<>();
        List<GridData> data = spreadsheet.getSheets().get(0).getData();
        if (data.isEmpty()) return List.of();

        List<RowData> rows = data.get(0).getRowData();
        if (rows == null) return List.of();

        for (RowData row : rows) {
            List<CellData> cells = row.getValues();
            if (cells == null || cells.size() < 6) continue;

            String fullName = getText(cells, 0); // Column C
            String nickName = getText(cells, 3); // Column F

            Map<LocalDate, ServiceStatus> calendar = new LinkedHashMap<>();

            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());
            int maxDay = yearMonth.lengthOfMonth();

            for (int day = 1; day <= maxDay; day++) {
                int colIndex = 6 + (day - 1); // I = index 6
                if (colIndex >= cells.size()) continue;

                CellData cell = cells.get(colIndex);
                String value = cell.getFormattedValue() != null ? cell.getFormattedValue() : "";
                Color bg = Optional.ofNullable(cell.getEffectiveFormat())
                        .map(CellFormat::getBackgroundColor)
                        .orElse(null);

                ServiceStatus status = ServiceStatus.from(value, bg);
                LocalDate dateForDay = yearMonth.atDay(day); // safe!
                calendar.put(dateForDay, status);
            }

            ContractorServiceStatusDto dto = new ContractorServiceStatusDto();
            String normalizedFullName = fullName.trim().toLowerCase();

            dto.setId(contractorService.getContractorByFullname(normalizedFullName));
            dto.setFullName(fullName);
            dto.setNickName(nickName);
            dto.setCalendar(calendar);

            results.add(dto);
        }
        return results;
    }

    private String getText(List<CellData> cells, int index) {
        return (index < cells.size() && cells.get(index).getFormattedValue() != null)
                ? cells.get(index).getFormattedValue()
                : "";
    }

}
