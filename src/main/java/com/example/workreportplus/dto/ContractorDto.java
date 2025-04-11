package com.example.workreportplus.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode
@Data
public class ContractorDto {

    private UUID id; // ✅ Added primary key field

    private String firstName;
    private String lastName;
    private String middleName;
    private String nickName;

    private String gender; // ✅ Matches DB `char` type with 'M' or 'F' constraint

    private LocalDate birthDate; // ✅ Changed from String to LocalDate
    private String nationality;
    private LocalDate dateOfArrivalToUnit; // ✅ Changed from String to LocalDate

    private String contractorStatus; // ✅ Matches DB `TEXT` type (ENUM was assumed)
    private String c_rank; // ✅ Matches DB `TEXT` type

    private UUID positionId; // ✅ Matches DB `position_id` foreign key
    private UUID unitId; // ✅ Matches DB `unit_id` foreign key

    private String createdBy;
    private LocalDateTime createdOn; // ✅ Matches DB `timestamp` type
    private String updatedBy;
    private LocalDateTime updatedOn; // ✅ Matches DB `timestamp` type

    private Boolean status; // ✅ Matches DB `BOOLEAN DEFAULT TRUE`
    private UUID groupId;
}
