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
 * 图书位置关联表
 */
@Table(schema = "book_location_rel")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookLocationRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private Long locationId;

    /**
     * 副本数
     */
    @Column(nullable = false)
    private Integer replicationAmount;

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

