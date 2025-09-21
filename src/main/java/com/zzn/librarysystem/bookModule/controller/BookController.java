package com.zzn.librarysystem.bookModule.controller;

import com.zzn.librarysystem.bookModule.dto.BookDetailInfoDto;
import com.zzn.librarysystem.bookModule.dto.BookInfoDto;
import com.zzn.librarysystem.bookModule.dto.SearchBookDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.service.BookService;
import com.zzn.librarysystem.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/books")
@PreAuthorize("hasAnyRole('NORMAL_USER')")
@CrossOrigin
public class BookController {

    private final BookService bookService;
    private final Mapper bookMapper;

    /**
     * 图书分页查询接口
     */
    @GetMapping
    public PagedResponse<BookInfoDto> search(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size,
            @ModelAttribute SearchBookDto searchBookDto) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(
                bookService.searchBooks(pageable, searchBookDto)
        );
    }

    /**
     * 图书详情查询接口
     */
    @GetMapping("/{id}")
    public BookDetailInfoDto getBookDetail(@PathVariable("id") Long bookId) {
        return bookService.getBookDetail(bookId);
    }

    /**
     * 图书推荐接口
     */
    @GetMapping("/recommended")
    public List<BookInfoDto> getRecommendedBooks(
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size) {
        return bookService.getRecommendedBooks(size);
    }

    /**
     * 图书类别查询
     */
    @GetMapping("/categories")
    public List<String> getCategories() {
        return bookService.getBookCategories();
    }
}
