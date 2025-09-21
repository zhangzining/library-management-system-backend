package com.zzn.librarysystem.bookModule.controller;

import com.zzn.librarysystem.bookModule.dto.BookSubscriptionDto;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionRequestDto;
import com.zzn.librarysystem.bookModule.service.SubscriptionService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/subscriptions")
@PreAuthorize("hasAnyRole('NORMAL_USER')")
@CrossOrigin
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    /**
     * 查看所有订阅
     */
    @GetMapping
    public PagedResponse<BookSubscriptionDto> getSubscriptions(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(subscriptionService.getSubscriptions(DataUtil.getCurrentUserId(), pageable));
    }

    /**
     * 订阅图书
     */
    @PostMapping
    public ResponseEntity<Void> subscribe(@RequestBody BookSubscriptionRequestDto requestDto) {
        requestDto.setUserId(DataUtil.getCurrentUserId());
        subscriptionService.subscribe(requestDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 取消订阅图书
     */
    @PutMapping
    public ResponseEntity<Void> unsubscribe(@RequestBody BookSubscriptionRequestDto requestDto) {
        requestDto.setUserId(DataUtil.getCurrentUserId());
        subscriptionService.unsubscribe(requestDto);
        return ResponseEntity.accepted().build();
    }
}
