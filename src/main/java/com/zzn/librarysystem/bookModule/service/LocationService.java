package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.dto.LocationDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final Mapper mapper;

    public Location createLocation(LocationDto locationDto) {
        Optional<Location> existed = locationRepository.findByBuildingNameAndBuildingLevelAndRoomNameAndShelfNumberAndShelfLevelNumber(
                locationDto.getBuildingName(),
                locationDto.getBuildingLevel(),
                locationDto.getRoomName(),
                locationDto.getShelfNumber(),
                locationDto.getShelfLevelNumber());
        return existed.orElseGet(() -> {
            Location location = mapper.map(locationDto, Location.class);
            return locationRepository.save(location);
        });
    }

    public Page<Location> getLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }
}
