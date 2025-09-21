package com.zzn.librarysystem.bookModule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Data
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class BookInfoDto {
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "作者不能为空")
    private String author;

    @NotBlank(message = "出报社不能为空")
    private String publisher;

    @NotBlank(message = "ISBN不能为空")
    private String isbn;

    @NotBlank(message = "索书号不能为空")
    private String indexNumber;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "语言不能为空")
    private String language;

    @NotBlank(message = "图片不能为空")
    private String coverImg;

    @Builder.Default
    private Integer availableReplicationAmount = 0;

    @NotNull(message = "总数不能为空")
    @Builder.Default
    private Integer totalReplicationAmount = 0;

    private Instant creationTime;

    private List<LocationDto> locations;

    /**
     * 是否已收藏
     */
    private Boolean hadCollected;
    /**
     * 收藏次数
     */
    private Integer collectedTimes;
    /**
     * 是否已订阅
     */
    private Boolean hadSubscribed;
    /**
     * 订阅次数
     */
    private Integer subscribedTimes;
    /**
     * 阅读状态
     */
    private String lendingStatus;
    /**
     * 借阅次数
     */
    private Integer lendingTimes;
}
