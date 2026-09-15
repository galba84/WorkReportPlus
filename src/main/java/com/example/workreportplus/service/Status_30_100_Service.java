package com.example.workreportplus.service;

import com.example.workreportplus.ENUM.ServiceStatus;
import com.example.workreportplus.Utils.DateUtils;
import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.ContractorRelocation;
import com.example.workreportplus.dto.ContractorServiceStatusDto;
import com.example.workreportplus.dto.RegionDto;
import com.google.api.services.sheets.v4.model.*;
import org.jooq.DSLContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        return getServiceStatusDtos(date, tableId, range);
    }

    @Cacheable("getDepartedContractors")
    public List<ContractorRelocation> getDepartedContractors(LocalDate date, UUID regionId) throws IOException {
        String tableId = regionTo100_30_Map.getOrDefault(regionId.toString(), null);
        if (tableId == null) return List.of();

        String currentMonthYear = DateUtils.formatUkrainianMonthYear(date);
        String currentRange = currentMonthYear + "!C9:AM";

        LocalDate previousDayDate = date.minusDays(1);
        String previousDayMonthYear = DateUtils.formatUkrainianMonthYear(previousDayDate);
        String previousRange = previousDayMonthYear + "!C9:AM";

        List<ContractorServiceStatusDto> currentDateDataset = getServiceStatusDtos(date, tableId, currentRange);
        List<ContractorServiceStatusDto> previousDateDataset = getServiceStatusDtos(previousDayDate, tableId, previousRange);

        List<ContractorServiceStatusDto> dropped = findStatusDropouts(
                previousDateDataset, currentDateDataset, previousDayDate, date);

        return dropped.stream()
                .map(dto -> {
                    ContractorDto contractor = new ContractorDto();
                    contractor.setId(dto.getId());
                    contractor.setFirstName(dto.getFullName());
                    contractor.setNickName(dto.getNickName());
                    RegionDto regionDto = new RegionDto();
                    regionDto.setId(regionId.toString());
                    return new ContractorRelocation(
                            contractor,
                            regionDto,    // you should define RegionDto region beforehand
                            true,      // sent
                            false,     // returned
                            null,      // orderNumber
                            null       // orderDate
                    );
                })
                .collect(Collectors.toList());
    }



    public List<ContractorServiceStatusDto> findStatusDropouts(
            List<ContractorServiceStatusDto> previousDateDataset,
            List<ContractorServiceStatusDto> currentDateDataset,
            LocalDate previousDayDate,
            LocalDate currentDayDate) {

        // Create a map from current dataset for quick lookup by ID
        Map<UUID, ContractorServiceStatusDto> currentMap = currentDateDataset.stream()
                .filter(dto -> dto.getId() != null) // фільтруємо null
                .collect(Collectors.toMap(
                        ContractorServiceStatusDto::getId,
                        Function.identity(),
                        (a, b) -> a // або b – залежно від того, який дубль залишити
                ));

        // Define statuses that indicate presence
        EnumSet<ServiceStatus> presentStatuses = EnumSet.of(
                ServiceStatus.FIRSTDAY,
                ServiceStatus.THIRTY,
                ServiceStatus.HUNDRED,
                ServiceStatus.L
        );

        List<ContractorServiceStatusDto> droppedOut = new ArrayList<>();

        for (ContractorServiceStatusDto previousDto : previousDateDataset) {
            ServiceStatus previousStatus = previousDto.getCalendar().getOrDefault(previousDayDate, ServiceStatus.UNKNOWN);

            if (presentStatuses.contains(previousStatus)) {
                ContractorServiceStatusDto currentDto = currentMap.get(previousDto.getId());

                if (currentDto != null) {
                    ServiceStatus currentStatus = currentDto.getCalendar().getOrDefault(currentDayDate, ServiceStatus.UNKNOWN);
                    if (currentStatus == ServiceStatus.NO) {
                        droppedOut.add(previousDto);
                    }
                }
            }
        }

        return droppedOut;
    }


    private List<ContractorServiceStatusDto> getServiceStatusDtos(LocalDate date, String tableId, String currentRange) throws IOException {
        Spreadsheet spreadsheet = googleSheetsService.readSheetWithGrid(tableId, currentRange);

        List<ContractorServiceStatusDto> results = new ArrayList<>();
        if (spreadsheet.getSheets() == null || spreadsheet.getSheets().isEmpty()) return List.of();
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
