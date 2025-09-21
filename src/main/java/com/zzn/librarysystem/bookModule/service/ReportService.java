package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.BookSubscriptionTrend;
import com.zzn.librarysystem.bookModule.domain.BookTransitionHistory;
import com.zzn.librarysystem.bookModule.dto.ReportBookTrendDto;
import com.zzn.librarysystem.bookModule.dto.ReportSummaryDto;
import com.zzn.librarysystem.bookModule.repository.BookBorrowingRecordRepository;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.bookModule.repository.BookSubscriptionRecordRepository;
import com.zzn.librarysystem.bookModule.repository.BookTransitionHistoryRepository;
import com.zzn.librarysystem.common.enums.OperationType;
import com.zzn.librarysystem.common.enums.UserType;
import com.zzn.librarysystem.common.util.DataUtil;
import com.zzn.librarysystem.userModule.repository.NormalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final NormalUserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookBorrowingRecordRepository borrowingRecordRepository;
    private final BookSubscriptionRecordRepository subscriptionRecordRepository;
    private final BookTransitionHistoryRepository historyRepository;

    public ReportSummaryDto getReport() {
        var today = LocalDate.now();
        var sevenDaysAgo = DataUtil.localDateToInstant(today.minusDays(7));

        var bookSubscriptionCount = subscriptionRecordRepository.countUnFulfilledBook();
        var borrowedBookCount = borrowingRecordRepository.countBorrowingBooks();
        var totalBookCount = Long.valueOf(bookRepository.count()).intValue();
        var activeUserCount = userRepository.countActiveUsers(sevenDaysAgo);

        return ReportSummaryDto.builder()
                .bookSubscriptionCount(bookSubscriptionCount)
                .borrowedBookCount(borrowedBookCount)
                .totalBookCount(totalBookCount)
                .activeUserCount(activeUserCount)
                .borrowedBookTrendList(getBorrowedBookTrendList(today, sevenDaysAgo))
                .bookSubscriptionTrendList(getBookSubscriptionTrendList())
                .build();
    }

    private List<ReportBookTrendDto> getBorrowedBookTrendList(LocalDate today, Instant sevenDaysAgo) {
        // 将借阅记录按日期统计
        Map<LocalDate, Long> borrowCountMap = historyRepository.findByOperationAndOperatorTypeAndCreationTimeAfter(
                        OperationType.BORROW_BOOK,
                        UserType.NORMAL_USER,
                        sevenDaysAgo)
                .stream()
                .peek(BookTransitionHistory::mapCreationDate)
                .collect(Collectors.groupingBy(BookTransitionHistory::getCreationDate, Collectors.counting()));
        List<ReportBookTrendDto> resultList = new ArrayList<>();
        // 从6天前开始，查找计数
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            resultList.add(ReportBookTrendDto.builder()
                            .date(date)
                            .count(borrowCountMap.getOrDefault(date, 0L))
                    .build());
        }
        return resultList;
    }

    private List<ReportBookTrendDto> getBookSubscriptionTrendList() {
        return subscriptionRecordRepository.selectBookIdsOrderByUnFulfillCount()
                .stream()
                .sorted(Comparator.comparingInt(BookSubscriptionTrend::getAmount).reversed())
                .limit(6)
                .map(res -> {
                    ReportBookTrendDto trendDto = bookRepository.findById(res.getBookId())
                            .map(book -> ReportBookTrendDto
                                    .builder()
                                    .title(book.getTitle())
                                    .category(book.getCategory())
                                    .build())
                            .orElseThrow();
                    trendDto.setCount(res.getAmount().longValue());
                    return trendDto;
                })
                .toList();
    }
}
