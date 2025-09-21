package com.zzn.librarysystem.bookModule.controller.management;

import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.dto.*;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.service.BookManagementService;
import com.zzn.librarysystem.bookModule.service.BookService;
import com.zzn.librarysystem.bookModule.service.FileService;
import com.zzn.librarysystem.bookModule.service.LocationService;
import com.zzn.librarysystem.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/management/books")
@PreAuthorize("hasAnyRole('ADMIN_USER') and hasAnyAuthority('BOOK_MANAGE')")
@CrossOrigin
public class BookManagementController {

    private final BookManagementService bookManagementService;
    private final BookService bookService;
    private final FileService fileService;
    private final Mapper mapper;
    private final LocationService locationService;

    /**
     * 图书分页查询
     */
    @GetMapping
    public PagedResponse<BookInfoDto> getBooks(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size,
            @ModelAttribute SearchBookDto searchBookDto) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(
                bookManagementService.searchBooks(pageable, searchBookDto)
        );
    }

    /**
     * 创建图书
     */
    @PostMapping
    public ResponseEntity<Void> createBook(@RequestBody @Validated BookInfoDto bookInfoDto) {
        bookManagementService.createBook(bookInfoDto);
        return ResponseEntity.accepted().build();
    }

    /**
     * 修改图书
     */
    @PatchMapping("/{id}")
    public BookInfoDto updateBook(@PathVariable("id") Long bookId,
                                  @RequestBody @Validated BookInfoDto bookInfoDto) {
        bookInfoDto.setId(bookId);
        return bookManagementService.updateBook(bookInfoDto);
    }

    /**
     * 上架/下架图书
     */
    @DeleteMapping("/{id}")
    public Boolean deleteBook(@PathVariable("id") Long bookId) {
        return bookManagementService.toggleBook(bookId);
    }

    /**
     * 图书导入接口，通过上传包含图片（PNG/JPEG）和 EXCEL（xls/xlsx）的 ZIP 压缩包来导入图书
     *
     * @param file ZIP 文件
     * @return 导入结果
     */
    @PostMapping("/batch-import")
    public List<BookImportResultDto> importBooksByZipFile(@RequestParam("file") MultipartFile file) {
        return fileService.importBooksByZipFile(file);
    }

    /**
     * 上架/下架图书
     */
    @PostMapping("/{bookId}/locations")
    public ResponseEntity<Void> bindBookToLocation(
            @PathVariable("bookId") Long bookId,
            @RequestBody @Validated BookBindingDto bookBindingDto
    ) {
        bookBindingDto.setBookId(bookId);
        bookManagementService.bindBookToLocation(bookBindingDto);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{bookId}/place")
    public ResponseEntity<Void> bindBookToPlace(
            @PathVariable("bookId") Long bookId,
            @RequestBody LocationDto locationDto
    ) {
        Location location = locationService.createLocation(locationDto);
        locationDto.setId(location.getId());
        bookManagementService.bindBookToLocation(locationDto, bookId);
        return ResponseEntity.accepted().build();
    }
}
