package com.zzn.librarysystem.bookModule.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder=true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailInfoDto extends BookInfoDto{
    private List<LocationDto> locations;

}
