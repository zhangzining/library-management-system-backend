package com.zzn.librarysystem.userModule.repository;

import com.zzn.librarysystem.userModule.domain.AdminUser;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByIdAndClientId(Long id, String clientId);

    boolean existsByUsername(@NotBlank String username);

    List<AdminUser> findAllByUsernameLike(String username);
}
