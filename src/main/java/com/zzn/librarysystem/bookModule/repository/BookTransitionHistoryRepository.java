package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookTransitionHistory;
import com.zzn.librarysystem.common.enums.OperationType;
import com.zzn.librarysystem.common.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

public interface BookTransitionHistoryRepository extends JpaRepository<BookTransitionHistory, Long>, JpaSpecificationExecutor<BookTransitionHistory> {
    List<BookTransitionHistory> findByOperationAndOperatorTypeAndCreationTimeAfter(OperationType operationType, UserType userType, Instant creationTime);
    Integer countDistinctByOperationAndBookId(OperationType operationType, Long bookId);
}
