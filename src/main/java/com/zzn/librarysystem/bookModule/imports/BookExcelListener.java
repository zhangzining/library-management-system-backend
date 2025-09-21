package com.zzn.librarysystem.bookModule.imports;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.zzn.librarysystem.bookModule.service.BookImportService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class BookExcelListener extends AnalysisEventListener<BookImportModel> {

    private static final int BATCH_COUNT = 15;

    private final BookImportService bookImportService;

    private final String batchId;
    private final String filename;
    private final  Map<String, String> imageNameUidMap;

    private Integer failedTimes = 0;
    private Integer successTimes = 0;

    public static final ThreadLocal<ArrayList<BookImportModel>> cache = ThreadLocal.withInitial(ArrayList::new);

    public BookExcelListener(BookImportService bookImportService, String batchId, String filename, Map<String, String> imageNameUidMap) {
        this.bookImportService = bookImportService;
        this.batchId = batchId;
        this.filename = filename;
        this.imageNameUidMap = imageNameUidMap;
    }

    @Override
    public void invoke(BookImportModel book, AnalysisContext analysisContext) {
        // 缓存每一行
        ArrayList<BookImportModel> bookList = cache.get();
        bookList.add(book);

        // 缓存到batchCount后一次性写入
        if (bookList.size() >= BATCH_COUNT) {
            bookImportService.saveBooks(bookList, batchId, imageNameUidMap);
            successTimes += bookList.size();
            bookList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 确保导入没有遗漏
        ArrayList<BookImportModel> bookList = cache.get();
        if (!bookList.isEmpty()) {
            bookImportService.saveBooks(bookList, batchId, imageNameUidMap);
            successTimes += bookList.size();
            bookList.clear();
        }

        // 清除ThreadLocal
        cache.remove();

        bookImportService.saveBatchImportHistory(batchId, failedTimes, successTimes, filename);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("[BookExcelListener] Exception while parse excel", exception);
        failedTimes++;
    }

}
