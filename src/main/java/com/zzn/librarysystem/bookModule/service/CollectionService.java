package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.BookCollectionRecord;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionDto;
import com.zzn.librarysystem.bookModule.dto.BookSubscriptionRequestDto;
import com.zzn.librarysystem.bookModule.repository.BookCollectionRecordRepository;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
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
public class CollectionService {
    private final BookCollectionRecordRepository bookCollectionRecordRepository;
    private final BookRepository bookRepository;

    /**
     * 收藏图书
     */
    public void collectBook(BookSubscriptionRequestDto requestDto) {
        bookCollectionRecordRepository.findByBookIdAndUserId(requestDto.getBookId(), requestDto.getUserId())
                .orElseGet(() -> bookCollectionRecordRepository.save(BookCollectionRecord.builder()
                        .userId(requestDto.getUserId())
                        .bookId(requestDto.getBookId())
                        .build()));
    }

    /**
     * 取消收藏图书
     */
    public void releaseBook(BookSubscriptionRequestDto requestDto) {
        bookCollectionRecordRepository.deleteByBookIdAndUserId(requestDto.getBookId(), requestDto.getUserId());
    }

    /**
     * 查看所有收藏
     */
    public Page<BookSubscriptionDto> getCollections(Long userId, Pageable pageable) {
        Page<BookCollectionRecord> records = bookCollectionRecordRepository.findByUserId(userId, pageable);
        List<Long> bookIds = records.stream().map(BookCollectionRecord::getBookId).toList();
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
