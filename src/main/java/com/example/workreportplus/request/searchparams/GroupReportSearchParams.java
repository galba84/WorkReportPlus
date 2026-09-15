package com.example.workreportplus.request.searchparams;

import lombok.Data;

import java.time.LocalDate;
@Data
public class GroupReportSearchParams extends SearchParams {

    LocalDate  startDate;
    LocalDate endDate;
    String groupId;

}
