package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookBorrowingRecord;
import com.zzn.librarysystem.common.enums.BookBorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookBorrowingRecordRepository extends JpaRepository<BookBorrowingRecord, Long>, JpaSpecificationExecutor<BookBorrowingRecord> {
    Optional<BookBorrowingRecord> findFirstByBookIdAndUserIdAndStatusOrderByExpectedReturnTime(Long bookId, Long userId, BookBorrowingStatus status);
    List<BookBorrowingRecord> findAllByBookId(Long bookId);
    List<BookBorrowingRecord> findAllByBookIdAndStatus(Long bookId, BookBorrowingStatus status);

    @Query(nativeQuery = true, value = "select status from book_borrowing_record where book_id = ?1 and user_id = ?2 order by creation_time desc limit 1")
    Optional<BookBorrowingStatus> getLatestBorrowingStatus(Long bookId, Long userId);

    @Query(nativeQuery = true, value = "select count(distinct user_id) from book_borrowing_record where book_id = ?1")
    Integer countByBookId(Long bookId);

    @Query(nativeQuery = true, value = "select count(distinct book_id) from book_borrowing_record where status = 'BORROWED'")
    Integer countBorrowingBooks();
}
