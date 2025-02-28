package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.ContractorStatus;
import com.example.workreportplus.ENUM.Rank;
import com.example.workreportplus.dto.templates.TrackedDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ContractorDto extends TrackedDto {

    private String firstName;
    private String lastName;
    private String middleName;
    private String nickName;
    private String gender;
    private String birthDate;
    private String nationality;
    private String dateOfArrivalToUnit;
    private ContractorStatus contractorStatus;
    private Rank rank;
    private String position;
    private String unitId;

}
