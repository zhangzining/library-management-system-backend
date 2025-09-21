package com.zzn.librarysystem.bookModule.controller.management;

import com.zzn.librarysystem.bookModule.dto.BookLendingRequestDto;
import com.zzn.librarysystem.bookModule.dto.BookLendingStatusDto;
import com.zzn.librarysystem.bookModule.service.BookLendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/management/books/lending")
@PreAuthorize("hasAnyRole('ADMIN_USER') and hasAnyAuthority('LENDING_MANAGE')")
@CrossOrigin
public class BookLendingManagementController {
    private final BookLendingService bookLendingService;

    @GetMapping
    public List<BookLendingStatusDto> getBookLendingStatus(@RequestParam("isbn") String isbn) {
        return bookLendingService.getBookLeadingStatus(isbn);
    }

    /**
     * 借书
     */
    @PostMapping("/{bookId}")
    public ResponseEntity<Void> borrowBook(
            @PathVariable("bookId") Long bookId,
            @RequestBody @Validated BookLendingRequestDto requestDto) {
        requestDto.setBookId(bookId);
        bookLendingService.borrowBook(requestDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 还书
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<Void> returnBook(
            @PathVariable("bookId") Long bookId,
            @RequestBody @Validated BookLendingRequestDto requestDto) {
        requestDto.setBookId(bookId);
        bookLendingService.returnBook(requestDto);
        return ResponseEntity.accepted().build();
    }
}
