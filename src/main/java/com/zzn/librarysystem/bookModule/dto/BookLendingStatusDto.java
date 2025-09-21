package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookLendingStatusDto {
    private Long bookId;
    private String bookName;

    private Long locationId;
    private String locationDescription;
    private Integer replicationNumber;

    private Long userId;
    private String username;
    private Instant borrowTime;
}
