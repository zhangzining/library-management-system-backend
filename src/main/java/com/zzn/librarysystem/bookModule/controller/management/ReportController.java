package com.zzn.librarysystem.bookModule.controller.management;

import com.zzn.librarysystem.bookModule.dto.ReportSummaryDto;
import com.zzn.librarysystem.bookModule.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/management/report")
@PreAuthorize("hasAnyRole('ADMIN_USER')")
@CrossOrigin
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public ReportSummaryDto getReport() {
        return reportService.getReport();
    }
}
