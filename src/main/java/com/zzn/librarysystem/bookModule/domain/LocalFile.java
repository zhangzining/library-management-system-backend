package com.zzn.librarysystem.bookModule.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 文件信息表
 */
@Table(schema = "file")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LocalFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件名
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 文件类型
     */
    @Column(nullable = false, length = 20)
    private String mediaType;

    /**
     * 文件ID
     */
    @Column(nullable = false, length = 100)
    private String uid;

    /**
     * 文件MD5值，用于去重
     */
    @Column(nullable = false, length = 50)
    private String hash;

    @CreatedDate
    @Column
    private Instant creationTime;

    @CreatedBy
    @Column(length = 50)
    private String createBy;
}
