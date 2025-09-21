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
 * 消息通知表
 */
@Table(schema = "notification")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 通知标题
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 通知内容
     */
    @Column(nullable = false, length = 100)
    private String content;

    /**
     * 收件人
     */
    @Column(nullable = false)
    private Long receiver;

    /**
     * 是否已读
     */
    @Column(nullable = false)
    private Boolean hadRead = false;

    @CreatedDate
    @Column
    private Instant creationTime;
}
