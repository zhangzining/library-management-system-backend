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
 * 图书位置表
 */
@Table(schema = "location")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 建筑名，如 新校区图书馆
     */
    @Column(nullable = false, length = 50)
    private String buildingName;

    /**
     * 楼层，如 1 （楼）
     */
    @Column(nullable = false, length = 50)
    private String buildingLevel;

    /**
     * 房间名，如 101 （室）
     */
    @Column(length = 50)
    private String roomName;

    /**
     * 书架号，如 1 （号架）
     */
    @Column(nullable = false, length = 50)
    private String shelfNumber;

    /**
     * 书架层号，如 1 （层）
     */
    @Column(nullable = false, length = 50)
    private String shelfLevelNumber;

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

    public String getDescription() {
        return String.format("%s %s楼 %s室 %s号架 %s层", buildingName, buildingLevel, roomName, shelfLevelNumber, shelfNumber);
    }
}

