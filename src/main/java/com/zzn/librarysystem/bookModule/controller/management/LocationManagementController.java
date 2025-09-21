package com.zzn.librarysystem.bookModule.controller.management;

import com.zzn.librarysystem.bookModule.dto.LocationDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.service.LocationService;
import com.zzn.librarysystem.common.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/management/locations")
@PreAuthorize("hasAnyRole('ADMIN_USER')")
@CrossOrigin
public class LocationManagementController {
    private final LocationService locationService;
    private final Mapper mapper;

    @PostMapping
    public Long createLocation(@RequestBody LocationDto location) {
        return locationService.createLocation(location).getId();
    }

    @GetMapping
    public PagedResponse<LocationDto> getLocations(
            @RequestParam(value = "page", required = false, defaultValue = "0")
            Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10")
            Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("creationTime")));
        return PagedResponse.of(
                locationService.getLocations(pageable),
                item -> mapper.map(item, LocationDto.class)
        );
    }

    @DeleteMapping
    public void deleteLocation(@RequestParam(value = "id") Long id) {
        locationService.deleteLocation(id);
    }
}
