package com.zzn.librarysystem.bookModule.domain;

import com.zzn.librarysystem.common.enums.OperationType;
import com.zzn.librarysystem.common.enums.UserType;
import com.zzn.librarysystem.common.util.DataUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 图书流转记录
 */
@Table(schema = "book_transition_history")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BookTransitionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所借图书ID
     */
    @Column(nullable = false)
    private Long bookId;

    /**
     * 操作人ID
     */
    @Column(nullable = false, length = 50)
    private Long operatorId;

    /**
     * 操作人类型
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserType operatorType;

    /**
     * 操作名称
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OperationType operation;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

    @CreatedDate
    @Column
    private Instant creationTime;

    @Transient
    private transient LocalDate creationDate;

    public void mapCreationDate() {
        creationDate = DataUtil.instantToLocalDate(creationTime);
    }
}
