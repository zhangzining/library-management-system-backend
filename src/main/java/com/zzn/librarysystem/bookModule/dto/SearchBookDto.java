package com.zzn.librarysystem.bookModule.dto;

import lombok.Data;

@Data
public class SearchBookDto {
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String indexNumber;
    private String category;
    private Boolean borrowable;
}
