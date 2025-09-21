package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAllByIdIn(List<Long> ids);
    @Query(nativeQuery = true, value = "select b.id from book b")
    List<Long> findIds();

    @Query(nativeQuery = true, value = "select distinct b.category from book b")
    List<String> findCategory();
}
