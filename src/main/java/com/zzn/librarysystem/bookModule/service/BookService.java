package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.dto.BookDetailInfoDto;
import com.zzn.librarysystem.bookModule.dto.BookInfoDto;
import com.zzn.librarysystem.bookModule.dto.LocationDto;
import com.zzn.librarysystem.bookModule.dto.SearchBookDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.repository.*;
import com.zzn.librarysystem.common.enums.BookBorrowingStatus;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.enums.OperationType;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.common.util.DataUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCollectionRecordRepository collectionRecordRepository;
    private final BookBorrowingRecordRepository borrowingRecordRepository;
    private final BookSubscriptionRecordRepository subscriptionRecordRepository;
    private final BookLocationRelRepository bookLocationRelRepository;
    private final LocationRepository locationRepository;
    private final BookTransitionHistoryRepository historyRepository;
    private final Mapper bookMapper;

    /**
     * 图书分页动态查询
     */
    public Page<BookInfoDto> searchBooks(Pageable pageable, SearchBookDto searchBookDto) {
        return bookRepository.findAll(buildSpecification(searchBookDto), pageable)
                .map(book -> {
                    BookInfoDto dto = bookMapper.map(book, BookInfoDto.class);
                    enrichBookInfo(dto);
                    return dto;
                });
    }

    /**
     * 构建动态查询对象
     */
    private Specification<Book> buildSpecification(SearchBookDto searchBookDto) {
        return (root, cq, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (StringUtils.isNotBlank(searchBookDto.getTitle())) {
                conditions.add(cb.like(root.get("title"), "%" + searchBookDto.getTitle() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getAuthor())) {
                conditions.add(cb.like(root.get("author"), "%" + searchBookDto.getTitle() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getPublisher())) {
                conditions.add(cb.like(root.get("publisher"), "%" + searchBookDto.getPublisher() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getIndexNumber())) {
                conditions.add(cb.equal(root.get("indexNumber"), searchBookDto.getIndexNumber()));
            }
            if (StringUtils.isNotBlank(searchBookDto.getIsbn())) {
                conditions.add(cb.equal(root.get("isbn"), searchBookDto.getIsbn()));
            }
            if (StringUtils.isNotBlank(searchBookDto.getCategory())) {
                conditions.add(cb.equal(root.get("category"), searchBookDto.getCategory()));
            }
            Optional.ofNullable(searchBookDto.getBorrowable())
                    .ifPresent(borrowable -> {
                        if (borrowable) {
                            conditions.add(cb.greaterThan(root.get("availableReplicationAmount"), 0));
                        }
                    });

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }

    public BookDetailInfoDto getBookDetail(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        BookDetailInfoDto bookDto = bookMapper.map(book, BookDetailInfoDto.class);
        log.info("[getBookDetail] bookId:{}, bookDto:{}", bookId, bookDto);
        enrichBookInfo(bookDto);
        return bookDto;
    }

    public List<BookInfoDto> getRecommendedBooks(Integer size) {
        List<Long> ids = bookRepository.findIds();
        List<Long> selectedIds = DataUtil.randomSelect(ids, size);
        List<Book> books = bookRepository.findAllByIdIn(selectedIds);
        List<BookInfoDto> bookInfoDtos = bookMapper.mapAsList(books, BookInfoDto.class);
        bookInfoDtos.forEach(this::enrichBookInfo);
        return bookInfoDtos;
    }

    public List<String> getBookCategories() {
        return bookRepository.findCategory();
    }

    public void enrichBookInfo(BookInfoDto dto) {
        Long userId = DataUtil.getCurrentUserId();
        dto.setHadCollected(collectionRecordRepository.existsByBookIdAndUserId(dto.getId(), userId));
        dto.setCollectedTimes(collectionRecordRepository.countBookCollectTimes(dto.getId()));
        dto.setHadSubscribed(subscriptionRecordRepository.existsByBookIdAndUserId(dto.getId(), userId));
        dto.setSubscribedTimes(subscriptionRecordRepository.countBookSubscribeTimes(dto.getId()));
        dto.setLendingTimes(historyRepository.countDistinctByOperationAndBookId(OperationType.BORROW_BOOK, dto.getId()));
        dto.setLendingStatus(borrowingRecordRepository.getLatestBorrowingStatus(dto.getId(), userId)
                .map(status -> {
                    if (BookBorrowingStatus.RETURNED_OVERTIME.equals(status)) {
                        status = BookBorrowingStatus.RETURNED;
                    }
                    if (!BookBorrowingStatus.BORROWED.equals(status) && !BookBorrowingStatus.RETURNED.equals(status)) {
                        status = null;
                    }
                    return status;
                })
                .map(BookBorrowingStatus::name)
                .orElse(null));

        List<LocationDto> locationDtos = bookLocationRelRepository.findByBookId(dto.getId())
                .stream().map(item -> {
                    String description = locationRepository.findById(item.getLocationId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION)).getDescription();
                    return LocationDto.builder()
                            .description(description)
                            .id(item.getLocationId())
                            .replicationNumber(item.getReplicationAmount())
                            .build();
                }).collect(Collectors.toList());
        dto.setLocations(locationDtos);
    }

}
