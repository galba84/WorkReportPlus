package com.example.workreportplus.dto;

import com.example.workreportplus.ENUM.ServiceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * @author Alex Sereda
 * @date 27.05.2025 22:30
 */
@Data
public class ContractorServiceStatusDto {

    UUID id;
    String fullName;
    String nickName;
    Map<LocalDate, ServiceStatus> calendar;
}
