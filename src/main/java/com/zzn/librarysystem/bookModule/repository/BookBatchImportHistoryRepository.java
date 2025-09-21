package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookBatchImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookBatchImportHistoryRepository extends JpaRepository<BookBatchImportHistory, Long>, JpaSpecificationExecutor<BookBatchImportHistory> {
    Optional<BookBatchImportHistory> findByBatchId(String batchId);
}
