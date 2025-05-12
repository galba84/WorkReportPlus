package com.example.workreportplus.dto;

import lombok.Data;

@Data
public class ContractorRelocation {
    ContractorDto contractor;
    RegionDto region;
    boolean sent;
    boolean returned;
    String orderNumber;
    String orderDate;
}
