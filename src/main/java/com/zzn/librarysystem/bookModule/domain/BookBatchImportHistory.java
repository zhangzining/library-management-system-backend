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

@Table(schema = "book_batch_import_history")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookBatchImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图书批量导入编号
     */
    @Column(nullable = false)
    private String batchId;

    /**
     * 导入失败数量
     */
    @Column
    private Integer failedAmount;

    /**
     * 导入成功数量
     */
    @Column
    private Integer successAmount;

    /**
     * 导入文件名
     */
    @Column(length = 500)
    private String filename;

    @CreatedDate
    @Column
    private Instant creationTime;

    @CreatedBy
    @Column(length = 50)
    private String createBy;
}
