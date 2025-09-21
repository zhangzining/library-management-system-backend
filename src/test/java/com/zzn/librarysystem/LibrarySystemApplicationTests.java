package com.zzn.librarysystem;

import com.zzn.librarysystem.bookModule.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LibrarySystemApplicationTests {

    @Spy
    private FileService fileService = new FileService(null, null);

    @Test
    void test() {
    }

    @Test
    void contextLoads() {
    }

}
