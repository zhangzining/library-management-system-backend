package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.BookBorrowingRecord;
import com.zzn.librarysystem.bookModule.domain.BookLocationRel;
import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.dto.BookLendingRequestDto;
import com.zzn.librarysystem.bookModule.dto.BookLendingStatusDto;
import com.zzn.librarysystem.bookModule.repository.BookBorrowingRecordRepository;
import com.zzn.librarysystem.bookModule.repository.BookLocationRelRepository;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.bookModule.repository.LocationRepository;
import com.zzn.librarysystem.common.enums.BookBorrowingStatus;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.userModule.domain.NormalUser;
import com.zzn.librarysystem.userModule.repository.NormalUserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookLendingService {
    @Value("${book.default_borrowing_month}")
    private Integer defaultBorrowingDuration;

    private final BookBorrowingRecordRepository bookBorrowRecordRepository;
    private final BookLocationRelRepository bookLocationRelRepository;
    private final BookRepository bookRepository;
    private final LocationRepository locationRepository;
    private final NormalUserRepository normalUserRepository;
    private final BookTransitionHistoryService historyService;

    /**
     * 借阅图书
     */
    @Transactional(rollbackFor = ApiException.class)
    public void borrowBook(BookLendingRequestDto requestDto) {
        if (StringUtils.isNotBlank(requestDto.getUsername())) {
            requestDto.setUserId(normalUserRepository.findByUsername(requestDto.getUsername())
                    .map(NormalUser::getId)
                    .orElseThrow(() -> ApiException.of(FailedReason.USERNAME_NOT_EXISTS))
            );
        }

        // 检查重复借阅
        if (bookBorrowRecordRepository.findFirstByBookIdAndUserIdAndStatusOrderByExpectedReturnTime(
                        requestDto.getBookId(), requestDto.getUserId(), BookBorrowingStatus.BORROWED)
                .isPresent()) {
            throw ApiException.of(FailedReason.HAD_BORROWED_SAME_BOOK);
        }

        deductLocationReplicationAmount(requestDto.getLocationId(), requestDto.getBookId());
        deductBookAvailableReplicationAmount(requestDto.getBookId());
        BookBorrowingRecord record = BookBorrowingRecord.builder()
                .bookId(requestDto.getBookId())
                .userId(requestDto.getUserId())
                .borrowTime(Instant.now())
                .expectedReturnTime(Instant.now().plus(defaultBorrowingDuration * 30, ChronoUnit.DAYS))
                .status(BookBorrowingStatus.BORROWED)
                .build();
        bookBorrowRecordRepository.save(record);

        // 记录借阅行为
        historyService.logNormalUserBorrowBook(record.getBookId(), record.getUserId());
    }

    /**
     * 减少图书可借库存
     */
    private void deductBookAvailableReplicationAmount(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        // 如果图书可借数量不足
        if (book.getAvailableReplicationAmount() <= 0) {
            throw ApiException.of(FailedReason.SUFFICIENT_BOOK_FOR_LENDING);
        }
        book.setAvailableReplicationAmount(book.getAvailableReplicationAmount() - 1);
        bookRepository.save(book);
    }

    /**
     * 减少图书在架库存
     */
    private void deductLocationReplicationAmount(Long locationId, Long bookId) {
        BookLocationRel relation = bookLocationRelRepository.findByBookIdAndLocationId(bookId, locationId)
                .orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION));
        // 如果图书可借数量不足
        if (relation.getReplicationAmount() <= 0) {
            throw ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION);
        }
        relation.setReplicationAmount(relation.getReplicationAmount() - 1);
        bookLocationRelRepository.save(relation);
    }

    /**
     * 归还图书
     */
    @Transactional(rollbackFor = ApiException.class)
    public void returnBook(BookLendingRequestDto requestDto) {
        BookBorrowingRecord record = bookBorrowRecordRepository.findFirstByBookIdAndUserIdAndStatusOrderByExpectedReturnTime(
                        requestDto.getBookId(), requestDto.getUserId(), BookBorrowingStatus.BORROWED)
                .orElseThrow(() -> ApiException.of(FailedReason.BORROWING_RECORD_NOT_EXISTS));

        record.setReturnTime(Instant.now());
        // 判断超期归还
        if (record.getReturnTime().isAfter(record.getExpectedReturnTime())) {
            record.setStatus(BookBorrowingStatus.RETURNED_OVERTIME);
        } else {
            record.setStatus(BookBorrowingStatus.RETURNED);
        }

        increaseBookAvailableReplicationAmount(requestDto.getBookId());
        bookBorrowRecordRepository.save(record);
        // 记录归还行为
        historyService.logNormalUserReturnBook(record.getBookId(), record.getUserId());
    }

    /**
     * 增加图书可借库存
     */
    private void increaseBookAvailableReplicationAmount(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        book.setAvailableReplicationAmount(book.getAvailableReplicationAmount() + 1);
        bookRepository.save(book);
    }

    /**
     * 查询图书借阅状态
     */
    public List<BookLendingStatusDto> getBookLeadingStatus(String isbn) {
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        List<BookLendingStatusDto> resultList = new ArrayList<>();
        // 查询可借状态
        List<BookLocationRel> locationRelations = bookLocationRelRepository.findByBookId(book.getId());
        if (locationRelations.isEmpty()) {
            resultList.add(BookLendingStatusDto.builder()
                    .bookId(book.getId())
                    .bookName(book.getTitle())
                    .replicationNumber(0)
                    .build());
        } else {
            locationRelations.forEach(relation -> {
                Location location = locationRepository.findById(relation.getLocationId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION));
                resultList.add(BookLendingStatusDto.builder()
                        .bookId(book.getId())
                        .bookName(book.getTitle())
                        .locationId(location.getId())
                        .locationDescription(location.getDescription())
                        .replicationNumber(relation.getReplicationAmount())
                        .build());
            });
        }

        // 查询出借状态
        List<BookBorrowingRecord> borrowingRecords = bookBorrowRecordRepository.findAllByBookIdAndStatus(book.getId(), BookBorrowingStatus.BORROWED);
        borrowingRecords.forEach(record -> {
            NormalUser normalUser = normalUserRepository.findById(record.getUserId()).orElseThrow(() -> ApiException.of(FailedReason.USERNAME_NOT_EXISTS));
            resultList.add(BookLendingStatusDto.builder()
                    .bookId(book.getId())
                    .bookName(book.getTitle())
                    .username(normalUser.getUsername())
                    .userId(normalUser.getId())
                    .borrowTime(record.getBorrowTime())
                    .build());
        });

        return resultList;
    }
}
