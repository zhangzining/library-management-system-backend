package com.zzn.librarysystem.bookModule.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationRequestDto {
    private String title;
    private String content;
    private List<Long> receiverIds;
}
