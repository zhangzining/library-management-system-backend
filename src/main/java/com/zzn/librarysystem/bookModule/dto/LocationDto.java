package com.zzn.librarysystem.bookModule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    private Long id;
    private String buildingName;
    private String buildingLevel;
    private String roomName;
    private String shelfNumber;
    private String shelfLevelNumber;

    private Integer replicationNumber;
    private String description;
}
