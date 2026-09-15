package com.example.workreportplus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report100Record {
    String contractorId;
    String name;
    String rank;
    String nickname;
    String groupName;
    LocalDate date;
    int coefficient;
}
