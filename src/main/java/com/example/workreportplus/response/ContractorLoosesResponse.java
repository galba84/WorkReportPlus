package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.PersonnelLossesType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ContractorLoosesResponse extends ContractorResponse {

    private PersonnelLossesType personnelLossesType;

    // ✅ Corrected Constructor
    public ContractorLoosesResponse(String name, PersonnelLossesType personnelLossesType) {
        super(name); // Pass name to parent class (ContractorResponse)
        this.personnelLossesType = personnelLossesType;
    }
}
