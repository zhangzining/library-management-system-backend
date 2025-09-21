package com.zzn.librarysystem.bookModule.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 图书收藏记录
 */
@Table(schema = "book_collection_record")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookCollectionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图书ID
     */
    @Column(nullable = false)
    private Long bookId;

    /**
     * 借阅人ID
     */
    @Column(nullable = false)
    private Long userId;


    @Column(nullable = false)
    @CreatedDate
    private Instant creationTime;
}
