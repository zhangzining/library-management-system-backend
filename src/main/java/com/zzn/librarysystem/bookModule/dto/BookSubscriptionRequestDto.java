package com.zzn.librarysystem.bookModule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookSubscriptionRequestDto {
    @NotNull
    private Long bookId;
    private Long userId;
}
