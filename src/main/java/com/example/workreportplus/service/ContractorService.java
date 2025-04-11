package com.example.workreportplus.service;

import com.example.jooq.tables.Contractor;
import com.example.jooq.tables.Positions;
import com.example.workreportplus.Utils.SecurityUtil;
import com.example.workreportplus.dto.ContractorDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.jooq.Tables.CONTRACTOR;

@Service
public class ContractorService {
    private final String DOVIDNYK_SHEET_ID = "1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss";
    private final GoogleSheetsService googleSheetsService;
    private final DSLContext dsl;

    public ContractorService(DSLContext dsl, GoogleSheetsService googleSheetsService) {
        this.dsl = dsl;
        this.googleSheetsService = googleSheetsService;
    }


    public ContractorDto getContractorById(UUID id) {
        return dsl.selectFrom(Contractor.CONTRACTOR)
                .where(CONTRACTOR.ID.eq(id))
                .fetchAnyInto(ContractorDto.class);
    }

    public List<ContractorDto> getContractors() {
        return dsl.selectFrom(CONTRACTOR)
                .fetchInto(ContractorDto.class);
    }

    public List<ContractorDto> getContractorsByGroupId(UUID id) {
        return dsl.selectFrom(CONTRACTOR)
                .where(CONTRACTOR.GROUP_ID.eq(id))
                .fetchInto(ContractorDto.class);
    }

    public void updateContractorsFromTable() throws IOException {
        String range = "NamesList!A2:I"; // adjust if needed
        List<List<Object>> lists = googleSheetsService.readSheet(DOVIDNYK_SHEET_ID, range);
        String currentUser = SecurityUtil.getCurrentUsername();

        // ⚠️ Remove Group loading, no longer needed
        // Load Position Name → List<ID> map (in case duplicates exist)
        Map<String, List<UUID>> positionNameToIds = dsl.select(Positions.POSITIONS.POSITION_NAME, Positions.POSITIONS.ID)
                .from(Positions.POSITIONS)
                .fetchGroups(Positions.POSITIONS.POSITION_NAME, Positions.POSITIONS.ID);

        // Map rows to DTOs
        List<ContractorDto> contractors = lists.stream()
                .filter(row -> row.size() >= 2)
                .map(row -> mapRowToDto(row, currentUser, positionNameToIds)) // remove groupNameToId param
                .toList();

        // Batch UPSERT stays unchanged
        dsl.batch(
                contractors.stream().map(dto ->
                        dsl.insertInto(CONTRACTOR)
                                .set(CONTRACTOR.ID, dto.getId())
                                .set(CONTRACTOR.FIRST_NAME, dto.getFirstName())
                                .set(CONTRACTOR.LAST_NAME, dto.getLastName())
                                .set(CONTRACTOR.MIDDLE_NAME, dto.getMiddleName())
                                .set(CONTRACTOR.NICK_NAME, dto.getNickName())
                                .set(CONTRACTOR.C_RANK, dto.getC_rank())
                                .set(CONTRACTOR.UNIT_ID, dto.getUnitId())
                                .set(CONTRACTOR.POSITION_ID, dto.getPositionId())
                                .set(CONTRACTOR.GENDER, dto.getGender())
                                .set(CONTRACTOR.BIRTH_DATE, dto.getBirthDate())
                                .set(CONTRACTOR.NATIONALITY, dto.getNationality())
                                .set(CONTRACTOR.DATE_OF_ARRIVAL_TO_UNIT, dto.getDateOfArrivalToUnit())
                                .set(CONTRACTOR.CONTRACTOR_STATUS, dto.getContractorStatus())
                                .set(CONTRACTOR.GROUP_ID, dto.getGroupId())
                                .set(CONTRACTOR.CREATED_BY, dto.getCreatedBy())
                                .set(CONTRACTOR.CREATED_ON, dto.getCreatedOn())
                                .set(CONTRACTOR.UPDATED_BY, dto.getUpdatedBy())
                                .set(CONTRACTOR.UPDATED_ON, dto.getUpdatedOn())
                                .set(CONTRACTOR.STATUS, dto.getStatus())
                                .onConflict(CONTRACTOR.ID)
                                .doUpdate()
                                .set(CONTRACTOR.FIRST_NAME, dto.getFirstName())
                                .set(CONTRACTOR.LAST_NAME, dto.getLastName())
                                .set(CONTRACTOR.MIDDLE_NAME, dto.getMiddleName())
                                .set(CONTRACTOR.NICK_NAME, dto.getNickName())
                                .set(CONTRACTOR.C_RANK, dto.getC_rank())
                                .set(CONTRACTOR.UNIT_ID, dto.getUnitId())
                                .set(CONTRACTOR.POSITION_ID, dto.getPositionId())
                                .set(CONTRACTOR.GENDER, dto.getGender())
                                .set(CONTRACTOR.BIRTH_DATE, dto.getBirthDate())
                                .set(CONTRACTOR.NATIONALITY, dto.getNationality())
                                .set(CONTRACTOR.DATE_OF_ARRIVAL_TO_UNIT, dto.getDateOfArrivalToUnit())
                                .set(CONTRACTOR.CONTRACTOR_STATUS, dto.getContractorStatus())
                                .set(CONTRACTOR.UPDATED_BY, dto.getUpdatedBy())
                                .set(CONTRACTOR.UPDATED_ON, dto.getUpdatedOn())
                                .set(CONTRACTOR.STATUS, dto.getStatus())
                                .set(CONTRACTOR.GROUP_ID, dto.getGroupId())

                ).toList()
        ).execute();
    }







    private ContractorDto mapRowToDto(List<Object> row, String currentUser, Map<String, List<UUID>> positionNameToIds) {
        ContractorDto dto = new ContractorDto();
        dto.setId(UUID.fromString(row.get(0).toString()));

        String[] nameParts = row.get(1).toString().trim().split("\\s+");
        dto.setLastName(nameParts.length > 0 ? nameParts[0] : "N/A");
        dto.setFirstName(nameParts.length > 1 ? nameParts[1] : "N/A");
        dto.setMiddleName(nameParts.length > 2 ? nameParts[2] : "N/A");

        dto.setC_rank(row.size() > 2 ? row.get(2).toString() : "N/A");
        dto.setNickName(row.size() > 4 ? row.get(4).toString() : "N/A");

        // ✅ Use unit_id directly
        dto.setUnitId(UUID.fromString(row.get(8).toString())); // assuming column 9 = unit_id
        dto.setGroupId(UUID.fromString(row.get(6).toString())); // assuming column 9 = group_id

        // ✅ Use position normally
        String positionName = row.size() > 3 ? row.get(3).toString().trim() : null;
        UUID positionId = Optional.ofNullable(positionNameToIds.get(positionName))
                .flatMap(ids -> ids.stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("Position not found: " + positionName));
        dto.setPositionId(positionId);

        dto.setGender("M");
        dto.setBirthDate(LocalDate.of(1970, 1, 1));
        dto.setNationality("N/A");
        dto.setDateOfArrivalToUnit(LocalDate.of(1970, 1, 1));
        dto.setContractorStatus("N/A");

        dto.setCreatedBy(currentUser);
        dto.setCreatedOn(LocalDateTime.now());
        dto.setUpdatedBy(currentUser);
        dto.setUpdatedOn(LocalDateTime.now());
        dto.setStatus(true);

        return dto;
    }


    public List<ContractorDto> getAllContractorByIds(List<UUID> contractorsIds) {
        if (contractorsIds == null || contractorsIds.isEmpty()) {
            return List.of();
        }

        return dsl.selectFrom(CONTRACTOR)
                .where(CONTRACTOR.ID.in(contractorsIds))
                .fetchInto(ContractorDto.class);
    }

}
