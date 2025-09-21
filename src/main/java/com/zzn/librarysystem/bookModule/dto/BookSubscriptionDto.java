package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookSubscriptionDto {
    private Long id;
    private Long bookId;
    private String bookName;
    private String bookCoverImg;
    private Instant creationTime;
}
