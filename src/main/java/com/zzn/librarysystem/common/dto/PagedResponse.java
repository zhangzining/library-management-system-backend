package com.zzn.librarysystem.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
public class PagedResponse<DTO> {
    @JsonProperty
    private final List<DTO> content;
    @JsonProperty
    private final PageMetaData page;

    private <ENTITY> PagedResponse(Page<ENTITY> page, Function<ENTITY, DTO> mapper) {
        this.content = page.map(mapper).getContent();
        this.page = new PageMetaData(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages());
    }

    public static <ENTITY, DTO> PagedResponse<DTO> of(Page<ENTITY> page, Function<ENTITY, DTO> mapper) {
        return new PagedResponse<>(page, mapper);
    }

    public static <DTO> PagedResponse<DTO> of(Page<DTO> page) {
        return new PagedResponse<>(page, (i) -> i);
    }

    @Getter
    public static class PageMetaData {
        @JsonProperty
        long size;
        @JsonProperty
        long number;
        @JsonProperty
        long totalElements;
        @JsonProperty
        long totalPages;

        public PageMetaData(long size, long number, long totalElements, long totalPages) {
            this.size = size;
            this.number = number;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}
