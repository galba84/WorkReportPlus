package com.example.workreportplus.dto.templates;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackedDto extends IdDto {
    @NotBlank
    private String createdBy;
    private LocalDate createdOn;
    @NotBlank
    private String updatedBy;
    private LocalDate updatedOn;
}
