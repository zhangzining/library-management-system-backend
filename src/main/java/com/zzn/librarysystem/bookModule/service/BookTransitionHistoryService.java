package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.BookTransitionHistory;
import com.zzn.librarysystem.bookModule.repository.BookTransitionHistoryRepository;
import com.zzn.librarysystem.common.enums.OperationType;
import com.zzn.librarysystem.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookTransitionHistoryService {
    private final BookTransitionHistoryRepository bookTransitionHistoryRepository;

    public void logNormalUserBorrowBook(Long bookId, Long userId) {
        saveTransition(builder ->
                builder.bookId(bookId)
                        .operatorId(userId)
                        .operation(OperationType.BORROW_BOOK)
                        .operatorType(UserType.NORMAL_USER)
        );
    }

    public void logNormalUserReturnBook(Long bookId, Long userId) {
        saveTransition(builder ->
                builder.bookId(bookId)
                        .operatorId(userId)
                        .operation(OperationType.RETURN_BOOK)
                        .operatorType(UserType.NORMAL_USER)
        );
    }

    public void logAdminUserPutOnBook(Long bookId, Long userId) {
        saveTransition(builder ->
                builder.bookId(bookId)
                        .operatorId(userId)
                        .operation(OperationType.PUT_ON_BOOK)
                        .operatorType(UserType.ADMIN_USER)
        );
    }

    public void logAdminUserTakeOffBook(Long bookId, Long userId) {
        saveTransition(builder ->
                builder.bookId(bookId)
                        .operatorId(userId)
                        .operation(OperationType.TAKE_OFF_BOOK)
                        .operatorType(UserType.ADMIN_USER)
        );
    }

    private void saveTransition(Consumer<BookTransitionHistory.BookTransitionHistoryBuilder> updater) {
        BookTransitionHistory.BookTransitionHistoryBuilder builder = BookTransitionHistory.builder();
        updater.accept(builder);
        bookTransitionHistoryRepository.save(builder.build());
    }
}
