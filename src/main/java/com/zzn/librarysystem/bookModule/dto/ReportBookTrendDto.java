package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReportBookTrendDto {
    private String title;
    private String category;
    private LocalDate date;
    private Long count;
}
