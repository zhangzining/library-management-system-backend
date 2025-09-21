package com.zzn.librarysystem.bookModule.dto;

import lombok.Data;

@Data
public class BookLendingRequestDto {

    private Long bookId;
    private Long locationId;
    private Long userId;
    private String username;
}
