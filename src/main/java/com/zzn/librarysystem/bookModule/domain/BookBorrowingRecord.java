package com.zzn.librarysystem.bookModule.domain;

import com.zzn.librarysystem.common.enums.BookBorrowingStatus;
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
 * 图书借阅记录
 */
@Table(schema = "book_borrowing_record")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookBorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所借图书ID
     */
    @Column(nullable = false)
    private Long bookId;

    /**
     * 借阅人ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 借出时间
     */
    @Column(nullable = false)
    private Instant borrowTime;

    /**
     * 期待归还时间
     */
    @Column(nullable = false)
    private Instant expectedReturnTime;

    /**
     * 实际归还时间
     */
    @Column
    private Instant returnTime;

    /**
     * 借阅状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookBorrowingStatus status;

    @Column(nullable = false)
    @CreatedDate
    private Instant creationTime;

    @CreatedBy
    @Column(length = 50)
    private String createBy;
}
