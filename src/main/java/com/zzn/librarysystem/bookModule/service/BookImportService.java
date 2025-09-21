package com.zzn.librarysystem.bookModule.service;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.BookBatchImportHistory;
import com.zzn.librarysystem.bookModule.dto.BookImportResultDto;
import com.zzn.librarysystem.bookModule.imports.BookExcelListener;
import com.zzn.librarysystem.bookModule.imports.BookImportModel;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.repository.BookBatchImportHistoryRepository;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookImportService {
    private final BookBatchImportHistoryRepository bookBatchImportHistoryRepository;
    private final BookRepository bookRepository;
    private final Mapper mapper;


    public BookImportResultDto importBookFromExcel(Path filePath, Map<String, String> imageNameUidMap) {
        String fileName = filePath.getFileName().toString();
        log.info("[BookImportService] Import book from excel {}", fileName);
        String batchId = UUID.randomUUID().toString().replace("-", "");

        // 1.获得一个工作簿对象
        ExcelReaderBuilder readWorkBook = EasyExcel.read(filePath.toFile(), BookImportModel.class, new BookExcelListener(this, batchId, fileName, imageNameUidMap));

        // 2.获得一个工作表对象，默认读取第一个工作表
        ExcelReaderSheetBuilder sheet = readWorkBook.sheet();

        // 3.读取工作表中的内容
        sheet.doRead();
        log.info("[BookImportService] Import book from excel successful");
        BookBatchImportHistory history = bookBatchImportHistoryRepository.findByBatchId(batchId).orElseThrow(() -> ApiException.of(FailedReason.IMPORT_FAILED));
        return BookImportResultDto.builder()
                .failedAmount(history.getFailedAmount())
                .successAmount(history.getSuccessAmount())
                .build();
    }

    public void saveBooks(ArrayList<BookImportModel> bookList, String batchId, Map<String, String> imageNameUidMap) {
        List<Book> books = mapper.mapAsList(bookList, Book.class);
        books.forEach(book -> {
            book.setBatchId(batchId);
            book.setCoverImg(imageNameUidMap.get(book.getTitle()));
        });

        saveAndMergeBooks(books);
    }

    private void saveAndMergeBooks(List<Book> books) {
        // 如果图书已经存在，直接增加总数
        books = books.stream()
                .map(book ->
                        bookRepository.findByIsbn(book.getIsbn())
                                .map(existedBook -> {
                                    existedBook.setTotalReplicationAmount(book.getTotalReplicationAmount() + existedBook.getTotalReplicationAmount());
                                    return existedBook;
                                })
                                .orElse(book))
                .toList();
        bookRepository.saveAll(books);
    }

    /**
     * 保存导入记录
     */
    public void saveBatchImportHistory(String batchId, Integer failedAmount, Integer successAmount, String filename) {
        BookBatchImportHistory history = BookBatchImportHistory.builder()
                .batchId(batchId)
                .filename(filename)
                .successAmount(successAmount)
                .failedAmount(failedAmount)
                .build();
        bookBatchImportHistoryRepository.save(history);
        log.info("[BookImportService] Import successful {}", history);
    }
}
