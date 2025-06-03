package com.example.workreportplus.service;

import com.example.workreportplus.dto.ContractorDto;
import com.example.workreportplus.dto.ContractorRelocation;
import com.example.workreportplus.dto.RegionDto;
import com.example.workreportplus.dto.ShpsShtatkaDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ShpsService {
    private final GoogleSheetsService googleSheetsService;
    private final RegionService regionService;

    public ShpsService(GoogleSheetsService googleSheetsService, RegionService regionService) {
        this.googleSheetsService = googleSheetsService;
        this.regionService = regionService;
    }

    public List<ContractorRelocation> getArrivedContractors(LocalDate date, UUID regionId) throws IOException {
        String range = "Штатка!A2:CG";
        List<List<Object>> rows = googleSheetsService.readSheet(GoogleSheetsService.SHPS_TABLE_ID, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in Штатка sheet");
            return List.of();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String formattedDate = date.format(formatter);
        List<String> regionNames = regionService.getRegionNames();
        String regionName = regionService.getRegionNameById(regionId);

        return rows.stream()
                .map(this::parseShtatka)
                .filter(e -> regionName.equals(e.getDestination_L_11()))
                .filter(e -> "відрядження БР".equals(e.getAbsenceReason_H_7()))
                .filter(e -> "ОР".equalsIgnoreCase(e.getReturnDate_K_10()))
                .filter(e -> formattedDate.equals(e.getDepartureDate_I_8()))
                .map(dto -> toContractorRelocation(dto, true))
                .toList();
    }

    public List<ContractorRelocation> getDeparturedContractors(LocalDate date, UUID regionId) throws IOException {
        String range = "Штатка!A2:S";
        List<List<Object>> rows = googleSheetsService.readSheet(GoogleSheetsService.SHPS_TABLE_ID, range);
        if (rows.isEmpty()) {
            System.out.println("No data found in Штатка sheet");
            return List.of();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String formattedDate = date.format(formatter);
        List<String> regionNames = regionService.getRegionNames();

        return rows.stream()
                .map(this::parseShtatka)
                .filter(e -> regionNames.contains(e.getDestination_L_11()))
                .filter(e -> "відрядження БР".equals(e.getAbsenceReason_H_7()))
                .filter(e -> containsDateInFormat(e.getReturnDate_K_10())) // skip "ОР"
                .filter(e -> formattedDate.equals(e.getReturnDate_K_10()))
                .map(dto -> toContractorRelocation(dto, false)) // in getArrivedContractors
                .toList();
    }


    private ShpsShtatkaDto parseShtatka(List<Object> row) {
        ShpsShtatkaDto dto = new ShpsShtatkaDto();
        dto.setNumber_A_0(safeGet(row, 0));
        dto.setId_B_1(safeGet(row, 1));
        dto.setRank_D_2(safeGet(row, 2));
        dto.setName_E_4(safeGet(row, 4));
        dto.setAbsenceReason_H_7(safeGet(row, 7));
        dto.setDepartureDate_I_8(safeGet(row, 8));
        dto.setReturnDate_K_10(safeGet(row, 10));
        dto.setDestination_L_11(safeGet(row, 11));
        dto.setOrderNumber_M_12(safeGet(row, 12));
        dto.setOrderDate_N_13(safeGet(row, 13));
        dto.setPosition_S_18(safeGet(row, 18));
        dto.setNickname_CG_84(safeGet(row, 84));
        return dto;
    }


    private String safeGet(List<Object> row, int index) {
        return index < row.size() ? (String) row.get(index) : null;
    }

    public boolean containsDateInFormat(String input) {
        return input != null && input.matches(".*\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b.*");
    }

    private ContractorRelocation toContractorRelocation(ShpsShtatkaDto dto, boolean isArrival) {
        ContractorRelocation relocation = new ContractorRelocation();

        relocation.setContractor(toContractorDto(dto));

        RegionDto region = regionService.getByName(dto.getDestination_L_11())
                .orElseThrow(() -> new IllegalStateException("Unknown region: " + dto.getDestination_L_11()));
        relocation.setRegion(region);

        relocation.setOrderNumber(dto.getOrderNumber_M_12());
        relocation.setOrderDate(dto.getOrderDate_N_13());
        relocation.setSent(isArrival);
        relocation.setReturned(!isArrival);

        return relocation;
    }

    private ContractorDto toContractorDto(ShpsShtatkaDto dto) {
        ContractorDto contractor = new ContractorDto();

        if (dto.getName_E_4() != null && !dto.getName_E_4().isBlank()) {
            String[] parts = dto.getName_E_4().trim().split("\\s+");

            if (parts.length >= 1) {
                contractor.setLastName(parts[0]);
            }
            if (parts.length >= 2) {
                contractor.setFirstName(parts[1]);
            }
            if (parts.length >= 3) {
                contractor.setMiddleName(parts[2]);
            }
        }
        contractor.setNickName(dto.getNickname_CG_84());
        contractor.setId(UUID.fromString(dto.getId_B_1())); // fallback if names are not split
        contractor.setC_rank(dto.getRank_D_2());
        contractor.setStatus(true);

        return contractor;
    }


}
