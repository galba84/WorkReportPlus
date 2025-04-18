package com.example.workreportplus.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ContractorWorkReportDtoRecord(UUID contractorId, LocalDate date, Boolean isWorked, String groupName) {}
