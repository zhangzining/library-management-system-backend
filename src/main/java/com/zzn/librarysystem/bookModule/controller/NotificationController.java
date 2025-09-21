package com.zzn.librarysystem.bookModule.controller;

import com.zzn.librarysystem.bookModule.dto.NotificationDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.service.NotificationService;
import com.zzn.librarysystem.common.dto.PagedResponse;
import com.zzn.librarysystem.common.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
@PreAuthorize("hasAnyRole('NORMAL_USER')")
@CrossOrigin
public class NotificationController {
    private final Mapper mapper;
    private final NotificationService notificationService;

//    @GetMapping
    public PagedResponse<NotificationDto> getNotifications(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("hadRead"), Sort.Order.asc("creationTime")));
        Long userId = DataUtil.getCurrentUserId();
        return PagedResponse.of(
                notificationService.getNotifications(userId, pageable),
                item -> mapper.map(item, NotificationDto.class)
        );
    }

    @GetMapping("/unreadCount")
    public Integer getUnreadCount() {
        Long userId = DataUtil.getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> readNotification(@PathVariable("id") Long id) {
        notificationService.readNotification(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public List<NotificationDto> listUnreadNotifications() {
        var userId = DataUtil.getCurrentUserId();
        return notificationService.listUnreadNotification(userId);
    }

    @PatchMapping
    public void readAll() {
        var userId = DataUtil.getCurrentUserId();
        notificationService.readAllNotification(userId);
    }
}
