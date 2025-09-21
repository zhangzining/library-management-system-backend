package com.zzn.librarysystem.bookModule.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class NotificationDto {
    private Long id;
    private String title;
    private String content;
    private Boolean hadRead;
    private Instant creationTime;
}
