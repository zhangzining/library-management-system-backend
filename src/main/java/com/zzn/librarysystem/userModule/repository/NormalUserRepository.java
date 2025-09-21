package com.zzn.librarysystem.userModule.repository;

import com.zzn.librarysystem.userModule.domain.NormalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface NormalUserRepository extends JpaRepository<NormalUser, Long> {

    Optional<NormalUser> findByUsername(String username);

    Optional<NormalUser> findByIdAndClientId(Long id, String clientId);

    Page<NormalUser> findAllByUsernameLike(String username, Pageable pageable);

    boolean existsByUsername(String username);

    @Query(nativeQuery = true, value = "select count(distinct id) from normal_user where status = 'ACTIVE' and last_login_time > ?1")
    Integer countActiveUsers(Instant lastLoginTimeStart);
}
