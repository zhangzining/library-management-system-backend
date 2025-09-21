package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.LocalFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LocalFileRepository extends JpaRepository<LocalFile, Long>, JpaSpecificationExecutor<LocalFile> {
    Optional<LocalFile> findByUid(String uid);

    Optional<LocalFile> findByHash(String hash);
}
