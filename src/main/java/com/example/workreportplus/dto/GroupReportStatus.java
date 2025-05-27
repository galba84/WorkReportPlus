package com.example.workreportplus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Alex Sereda
 * @date 23.05.2025 15:44
 */
@Data
@AllArgsConstructor
public class GroupReportStatus {
    String name;
    String status;
    String type;
    String ammunitionStatus;
}
