package com.zzn.librarysystem.bookModule.controller;

import com.zzn.librarysystem.bookModule.dto.BookSubscriptionDto;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionRequestDto;
import com.zzn.librarysystem.bookModule.service.CollectionService;
import com.zzn.librarysystem.common.dto.PagedResponse;
import com.zzn.librarysystem.common.util.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/collections")
@PreAuthorize("hasAnyRole('NORMAL_USER')")
@CrossOrigin
public class CollectionController {
    private final CollectionService collectionService;
    /**
     * 查看所有收藏
     */
    @GetMapping
    public PagedResponse<BookSubscriptionDto> getSubscriptions(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(collectionService.getCollections(DataUtil.getCurrentUserId(), pageable));
    }

    /**
     * 收藏图书
     */
    @PostMapping
    public ResponseEntity<Void> collectBook(@RequestBody @Validated BookSubscriptionRequestDto requestDto) {
        requestDto.setUserId(DataUtil.getCurrentUserId());
        collectionService.collectBook(requestDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 取消收藏图书
     */
    @DeleteMapping
    public ResponseEntity<Void> releaseBook(@RequestBody @Validated BookSubscriptionRequestDto requestDto) {
        requestDto.setUserId(DataUtil.getCurrentUserId());
        collectionService.releaseBook(requestDto);
        return ResponseEntity.accepted().build();
    }
}
