package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookCollectionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookCollectionRecordRepository extends JpaRepository<BookCollectionRecord, Long>, JpaSpecificationExecutor<BookCollectionRecord> {
    Optional<BookCollectionRecord> findByBookIdAndUserId(Long bookId, Long userId);

    Page<BookCollectionRecord> findByUserId(Long userId, Pageable pageable);

    @Modifying
    void deleteByBookIdAndUserId(Long bookId, Long userId);

    @Query(nativeQuery = true, value = "select count(distinct user_id) from book_collection_record where book_id = ?1")
    Integer countBookCollectTimes(Long bookId);

    Boolean existsByBookIdAndUserId(Long bookId, Long userId);
}
