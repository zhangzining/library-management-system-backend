package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.bookModule.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookDataMockService implements InitializingBean {

    private final BookRepository bookRepository;
    private final LocationRepository locationRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
//        initBookData();
        initLocationData();
    }

    private void initLocationData() {
        Location location = Location.builder()
                .id(1L)
                .buildingName("新校区图书馆")
                .buildingLevel("一")
                .roomName("1号阅览室")
                .shelfNumber("3")
                .shelfLevelNumber("2")
                .build();

        locationRepository.save(location);
        Location location2 = Location.builder()
                .id(2L)
                .buildingName("新校区图书馆")
                .buildingLevel("一")
                .roomName("3号阅览室")
                .shelfNumber("2")
                .shelfLevelNumber("4")
                .build();

        locationRepository.save(location2);

        Location location3 = Location.builder()
                .id(3L)
                .buildingName("老校区图书馆")
                .buildingLevel("三")
                .roomName("5号阅览室")
                .shelfNumber("22")
                .shelfLevelNumber("3")
                .build();

        locationRepository.save(location3);
    }

    private void initBookData() {
        Book book = Book.builder()
                .id(1L)
                .author("author")
                .title("title")
                .description("description")
                .publisher("publisher")
                .coverImg("coverImg")
                .isbn("isbn")
                .indexNumber("indexNumber")
                .category("category")
                .language("language")
                .totalReplicationAmount(10)
                .availableReplicationAmount(0)
                .enable(true)
                .build();

        bookRepository.save(book);
    }
}
