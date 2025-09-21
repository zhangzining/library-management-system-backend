package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookImportResultDto {
    private Integer failedAmount;
    private Integer successAmount;
}
