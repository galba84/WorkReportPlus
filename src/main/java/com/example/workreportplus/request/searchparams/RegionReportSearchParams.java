package com.example.workreportplus.request.searchparams;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegionReportSearchParams extends SearchParams {

    LocalDate  startDate;
    LocalDate endDate;
    String regionName;

}
