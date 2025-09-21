package com.zzn.librarysystem.bookModule.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 图书信息表
 */
@Table(schema = "book")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图书批量导入编号
     */
    @Column(length = 50)
    private String batchId;

    /**
     * 书名
     */
    @Column(nullable = false, length = 50)
    private String title;

    /**
     * 简介
     */
    @Column(nullable = false, length = 1000)
    private String description;

    /**
     * 作者
     */
    @Column(nullable = false, length = 50)
    private String author;

    /**
     * 出版社
     */
    @Column(length = 50)
    private String publisher;

    /**
     * 封面图片
     */
    @Column(length = 50)
    private String coverImg;

    /**
     * ISBN 号
     */
    @Column(nullable = false, length = 50)
    private String isbn;

    /**
     * 索书号
     */
    @Column(nullable = false, length = 50)
    private String indexNumber;

    /**
     * 分类
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 语言
     */
    @Column(nullable = false, length = 50)
    private String language;

    /**
     * 可借副本数
     */
    @Column(nullable = false)
    private Integer availableReplicationAmount = 0;

    /**
     * 总副本数
     */
    @Column(nullable = false)
    private Integer totalReplicationAmount = 0;

    /**
     * 是否可借阅
     */
    @Column(nullable = false)
    private Boolean enable = true;

    @CreatedDate
    @Column
    private Instant creationTime;

    @CreatedBy
    @Column(length = 50)
    private String createBy;

    @LastModifiedDate
    @Column
    private Instant updateTime;

    @LastModifiedBy
    @Column(length = 50)
    private String updateBy;
}
