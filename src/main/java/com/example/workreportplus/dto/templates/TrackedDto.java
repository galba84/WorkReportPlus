package com.example.workreportplus.dto.templates;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrackedDto extends IdDto {
    @NotBlank
    private String createdBy;
    private LocalDate createdOn;
    @NotBlank
    private String updatedBy;
    private LocalDate updatedOn;
}
