package com.zzn.librarysystem.userModule.domain;

import com.zzn.librarysystem.common.enums.AdminUserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

@Table(schema = "admin_user")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AdminUser {

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
    @Column(nullable = false, unique = true, length = 500)
    private String password;

    /**
     * Client id for identify the user's belonging
     */
    @Column(nullable = false, length = 16)
    private String clientId;

    /**
     * User status
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AdminUserStatus status;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "admin_user_role_rel",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<AdminRole> roles;

    @Column(nullable = false)
    @CreatedDate
    private Instant creationTime;

    @Column
    private Instant lastLoginTime;
}
