package com.zzn.librarysystem.userModule.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(schema = "admin_role")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    public AdminRole(String name) {
        this.name = name;
    }
}
