package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.BookLocationRel;
import com.zzn.librarysystem.bookModule.domain.BookSubscriptionRecord;
import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionDto;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionRequestDto;
import com.zzn.librarysystem.bookModule.dto.NotificationRequestDto;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.bookModule.repository.BookSubscriptionRecordRepository;
import com.zzn.librarysystem.bookModule.repository.LocationRepository;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final BookSubscriptionRecordRepository subscriptionRecordRepository;
    private final NotificationService notificationService;
    private final BookRepository bookRepository;
    private final LocationRepository locationRepository;

    /**
     * 创建订阅记录
     */
    public void subscribe(BookSubscriptionRequestDto requestDto) {
        subscriptionRecordRepository.findByBookIdAndUserId(requestDto.getBookId(), requestDto.getUserId())
                .orElseGet(() -> subscriptionRecordRepository.save(BookSubscriptionRecord.builder()
                        .userId(requestDto.getUserId())
                        .bookId(requestDto.getBookId())
                        .fullFilled(false)
                        .build()));
    }

    /**
     * 取消订阅
     */
    public void unsubscribe(BookSubscriptionRequestDto requestDto) {
        subscriptionRecordRepository.deleteByBookIdAndUserId(requestDto.getBookId(), requestDto.getUserId());
    }

    /**
     * 订阅被满足，发送通知
     */
    public void subscriptionFulfilled(BookLocationRel bookLocationRel) {
        List<BookSubscriptionRecord> subscriptions = subscriptionRecordRepository.findByBookId(bookLocationRel.getBookId());
        Book book = bookRepository.findById(bookLocationRel.getBookId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        Location location = locationRepository.findById(bookLocationRel.getLocationId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION));

        String title = String.format("《%s》已上架", book.getTitle());
        String content = String.format("《%s》已上架至：%s", book.getTitle(), location.getDescription());
        List<Long> receiverIds = subscriptions.stream().map(BookSubscriptionRecord::getUserId).toList();

        notificationService.sendNotification(NotificationRequestDto.builder()
                .title(title)
                .content(content)
                .receiverIds(receiverIds)
                .build());
    }

    /**
     * 查看订阅
     */
    public Page<BookSubscriptionDto> getSubscriptions(Long userId, Pageable pageable) {
        Page<BookSubscriptionRecord> records = subscriptionRecordRepository.findByUserId(userId, pageable);
        List<Long> bookIds = records.stream().map(BookSubscriptionRecord::getBookId).toList();
        final Map<Long, List<Book>> bookMap = bookRepository.findAllByIdIn(bookIds).stream().collect(Collectors.groupingBy(Book::getId));
        return records.map(item -> {
            Book book = bookMap.get(item.getBookId()).get(0);
            return BookSubscriptionDto.builder()
                    .id(item.getId())
                    .bookId(item.getBookId())
                    .bookName(book.getTitle())
                    .bookCoverImg(book.getCoverImg())
                    .creationTime(item.getCreationTime())
                    .build();
        });
    }
}
