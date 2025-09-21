package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookSubscriptionRecord;
import com.zzn.librarysystem.bookModule.domain.BookSubscriptionTrend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookSubscriptionRecordRepository extends JpaRepository<BookSubscriptionRecord, Long>, JpaSpecificationExecutor<BookSubscriptionRecord> {
    Optional<BookSubscriptionRecord> findByBookIdAndUserId(Long bookId, Long userId);

    List<BookSubscriptionRecord> findByBookId(Long bookId);

    Page<BookSubscriptionRecord> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByBookIdAndUserId(Long bookId, Long userId);

    @Query(nativeQuery = true, value = "select count(distinct user_id) from book_subscription_record where book_id = ?1")
    Integer countBookSubscribeTimes(Long bookId);

    Boolean existsByBookIdAndUserId(Long bookId, Long userId);

    @Query(nativeQuery = true, value = "select count(distinct book_id) from book_subscription_record where full_filled = 0")
    Integer countUnFulfilledBook();

    @Query(nativeQuery = true, value = "select distinct book_id, count(distinct user_id) as amount from book_subscription_record where full_filled = 0 group by book_id")
    List<BookSubscriptionTrend> selectBookIdsOrderByUnFulfillCount();
}
