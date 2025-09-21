package com.zzn.librarysystem.bookModule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookBindingDto {
    /**
     * 位置ID
     */
    @NotNull
    private Long locationId;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 绑定/解绑 数量
     */
    @NotNull
    private Integer placeAmount;

    /**
     * 是否绑定（true：绑定，false：解绑）
     */
    private Boolean binding;
}
