package com.zzn.librarysystem.bookModule.imports;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookImportModel {
    @ExcelProperty("书名")
    private String title;
    @ExcelProperty("描述")
    private String description;
    @ExcelProperty("作者")
    private String author;
    @ExcelProperty("出版社")
    private String publisher;
    @ExcelProperty("ISBN")
    private String isbn;
    @ExcelProperty("索书号")
    private String indexNumber;
    @ExcelProperty("图书分类")
    private String category;
    @ExcelProperty("语言")
    private String language;
    @ExcelProperty("数量")
    private Integer totalReplicationAmount = 0;
}
