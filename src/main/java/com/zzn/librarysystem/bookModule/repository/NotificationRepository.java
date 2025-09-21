package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByReceiver(Long userId, Pageable pageable);
    Integer countByReceiverAndHadRead(Long userId, Boolean hadRead);
    List<Notification> findByReceiverAndHadRead(Long userId, Boolean hadRead);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update notification set had_read = 1 where receiver = ?1")
    void readAllByReceiver(Long userId);
}
