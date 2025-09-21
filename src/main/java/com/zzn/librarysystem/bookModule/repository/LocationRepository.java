package com.zzn.librarysystem.bookModule.repository;

import com.zzn.librarysystem.bookModule.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    List<Location> findAllByIdIn(List<Long> ids);

    @Query(nativeQuery = true, value =
            "select l.* where location l where l.id in (select r.location_id from book_location_rel r where r.book_id = ?1)")
    List<Location> findAllByBookId(Long bookId);

    Optional<Location> findByBuildingNameAndBuildingLevelAndRoomNameAndShelfNumberAndShelfLevelNumber(
            String buildingName, String buildingLevel, String roomName, String shelfNumber, String shelfLevelNumber);
}
