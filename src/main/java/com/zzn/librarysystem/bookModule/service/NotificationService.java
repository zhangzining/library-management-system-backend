package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Notification;
import com.zzn.librarysystem.bookModule.dto.NotificationDto;
import com.zzn.librarysystem.bookModule.dto.NotificationRequestDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.repository.NotificationRepository;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final Mapper mapper;

    /**
     * 查询所有消息
     */
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByReceiver(userId, pageable);
    }

    public Integer getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverAndHadRead(userId, false);
    }

    /**
     * 发送通知
     */
    public void sendNotification(NotificationRequestDto requestDto) {
        List<Notification> notifications = requestDto.getReceiverIds().stream().map(receiver -> Notification.builder()
                        .title(requestDto.getTitle())
                        .content(requestDto.getContent())
                        .receiver(receiver)
                        .hadRead(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    /**
     * 标记已读
     */
    public void readNotification(Long notificationId) {
        notificationRepository.findById(notificationId)
                .map(record -> {
                            record.setHadRead(true);
                            return notificationRepository.save(record);
                        }
                ).orElseThrow(() -> ApiException.of(FailedReason.NOTIFICATION_NOT_EXISTS));
    }

    public List<NotificationDto> listUnreadNotification(Long userId) {
        return mapper.mapAsList(notificationRepository.findByReceiverAndHadRead(userId, false), NotificationDto.class);
    }

    public void readAllNotification(Long userId) {
        notificationRepository.readAllByReceiver(userId);
    }
}
