package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportSummaryDto {
    private Integer bookSubscriptionCount;
    private Integer borrowedBookCount;
    private Integer totalBookCount;
    private Integer activeUserCount;
    private List<ReportBookTrendDto> borrowedBookTrendList;
    private List<ReportBookTrendDto> bookSubscriptionTrendList;
}
