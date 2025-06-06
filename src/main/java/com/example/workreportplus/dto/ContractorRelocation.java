package com.example.workreportplus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractorRelocation {
    ContractorDto contractor;
    RegionDto region;
    boolean sent;
    boolean returned;
    String orderNumber;
    String orderDate;
}
