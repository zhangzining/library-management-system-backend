package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.BookLocationRel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookLocationRelRepository extends JpaRepository<BookLocationRel, Long>, JpaSpecificationExecutor<BookLocationRel> {
    List<BookLocationRel> findByBookId(Long bookId);

    Optional<BookLocationRel> findByBookIdAndLocationId(Long bookId, Long locationId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from book_location_rel where replication_amount = 0")
    void clearEmptyRel();
}
