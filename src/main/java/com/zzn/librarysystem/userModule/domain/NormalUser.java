package com.zzn.librarysystem.userModule.domain;

import com.zzn.librarysystem.common.enums.NormalUserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Table(schema = "normal_user")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NormalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User name for login
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * User password for login
     */
    @Column(nullable = false, length = 500)
    private String password;

    /**
     * Client id for identify the user's belonging
     */
    @Column(nullable = false, length = 16)
    private String clientId;

    /**
     * User nickname for identify himself
     */
    @Column(length = 200)
    private String nickname;

    /**
     * User email
     */
    @Column(length = 200)
    private String email;

    /**
     * User avatar
     */
    @Column(length = 50)
    private String avatar;

    /**
     * User status
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NormalUserStatus status;

    @Column(nullable = false)
    @CreatedDate
    private Instant creationTime;

    @Column
    private Instant lastLoginTime;
}
