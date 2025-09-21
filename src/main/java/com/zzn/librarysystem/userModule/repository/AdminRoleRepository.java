package com.zzn.librarysystem.userModule.repository;

import com.zzn.librarysystem.userModule.domain.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByName(String name);
}
