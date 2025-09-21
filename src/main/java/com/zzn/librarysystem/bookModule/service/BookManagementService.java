package com.zzn.librarysystem.bookModule.service;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.BookLocationRel;
import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.dto.BookBindingDto;
import com.zzn.librarysystem.bookModule.dto.BookInfoDto;
import com.zzn.librarysystem.bookModule.dto.LocationDto;
import com.zzn.librarysystem.bookModule.dto.SearchBookDto;
import com.zzn.librarysystem.bookModule.mapper.Mapper;
import com.zzn.librarysystem.bookModule.repository.BookLocationRelRepository;
import com.zzn.librarysystem.bookModule.repository.BookRepository;
import com.zzn.librarysystem.bookModule.repository.LocationRepository;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import com.zzn.librarysystem.common.util.DataUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zzn.librarysystem.common.util.DataUtil.applyIfNotBlank;
import static com.zzn.librarysystem.common.util.DataUtil.applyIfNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookManagementService {
    private final BookRepository bookRepository;
    private final BookLocationRelRepository bookLocationRelRepository;
    private final Mapper bookMapper;
    private final LocationRepository locationRepository;
    private final SubscriptionService subscriptionService;
    private final BookTransitionHistoryService historyService;

    /**
     * 图书分页动态查询
     */
    public Page<BookInfoDto> searchBooks(Pageable pageable, SearchBookDto searchBookDto) {
        return bookRepository.findAll(buildSpecification(searchBookDto), pageable)
                .map(book -> {
                    BookInfoDto dto = bookMapper.map(book, BookInfoDto.class);
                    enrichBookInfo(dto);
                    return dto;
                });
    }

    private void enrichBookInfo(BookInfoDto dto) {
        List<LocationDto> locationDtos = bookLocationRelRepository.findByBookId(dto.getId())
                .stream().map(item -> {
                    var location = locationRepository.findById(item.getLocationId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_IN_LOCATION));
                    var locationDto = bookMapper.map(location, LocationDto.class);
                    locationDto.setReplicationNumber(item.getReplicationAmount());
                    return locationDto;
                }).collect(Collectors.toList());
        dto.setLocations(locationDtos);
    }

    /**
     * 构建动态查询对象
     */
    private Specification<Book> buildSpecification(SearchBookDto searchBookDto) {
        return (root, cq, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (StringUtils.isNotBlank(searchBookDto.getTitle())) {
                conditions.add(cb.like(root.get("title"), "%" + searchBookDto.getTitle() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getAuthor())) {
                conditions.add(cb.like(root.get("author"), "%" + searchBookDto.getTitle() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getPublisher())) {
                conditions.add(cb.like(root.get("publisher"), "%" + searchBookDto.getPublisher() + "%"));
            }
            if (StringUtils.isNotBlank(searchBookDto.getIndexNumber())) {
                conditions.add(cb.equal(root.get("indexNumber"), searchBookDto.getIndexNumber()));
            }
            if (StringUtils.isNotBlank(searchBookDto.getIsbn())) {
                conditions.add(cb.equal(root.get("isbn"), searchBookDto.getIsbn()));
            }
            if (StringUtils.isNotBlank(searchBookDto.getCategory())) {
                conditions.add(cb.equal(root.get("category"), searchBookDto.getCategory()));
            }
            Optional.ofNullable(searchBookDto.getBorrowable())
                    .ifPresent(borrowable -> {
                        if (borrowable) {
                            conditions.add(cb.greaterThan(root.get("availableReplicationAmount"), 0));
                        }
                    });

            return cb.and(conditions.toArray(new Predicate[0]));
        };
    }

    /**
     * 创建图书
     */
    public void createBook(BookInfoDto bookInfoDto) {
        Book book = bookMapper.map(bookInfoDto, Book.class);
        book.setAvailableReplicationAmount(0);
        book.setEnable(true);
        log.debug("[CreateBook] create book : {}", book);
        bookRepository.save(book);
    }

    /**
     * 修改图书
     */
    public BookInfoDto updateBook(BookInfoDto bookInfoDto) {
        Book existedBook = bookRepository.findById(bookInfoDto.getId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        updateBookProperty(bookInfoDto, existedBook);
        return bookMapper.map(bookRepository.save(existedBook), BookInfoDto.class);
    }

    private void updateBookProperty(BookInfoDto bookInfoDto, Book existedBook) {
        applyIfNotBlank(bookInfoDto::getTitle, existedBook::setTitle);
        applyIfNotBlank(bookInfoDto::getDescription, existedBook::setDescription);
        applyIfNotBlank(bookInfoDto::getAuthor, existedBook::setAuthor);
        applyIfNotBlank(bookInfoDto::getPublisher, existedBook::setPublisher);
        applyIfNotBlank(bookInfoDto::getIsbn, existedBook::setIsbn);
        applyIfNotBlank(bookInfoDto::getCoverImg, existedBook::setCoverImg);
        applyIfNotBlank(bookInfoDto::getIndexNumber, existedBook::setIndexNumber);
        applyIfNotBlank(bookInfoDto::getLanguage, existedBook::setLanguage);
        applyIfNotNull(bookInfoDto::getTotalReplicationAmount, existedBook::setTotalReplicationAmount);
    }

    /**
     * 上架/下架图书(全局)
     */
    public boolean toggleBook(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    book.setEnable(Boolean.FALSE.equals(book.getEnable()));
                    bookRepository.save(book);
                    return book.getEnable();
                })
                .orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
    }

    /**
     * 上架/下架图书
     */
    public void bindBookToLocation(BookBindingDto bookBindingDto) {
        Book book = bookRepository.findById(bookBindingDto.getBookId()).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        int replicationAmountCanBeBind = book.getTotalReplicationAmount() - book.getAvailableReplicationAmount();

        if (replicationAmountCanBeBind < 0) {
            throw ApiException.of(FailedReason.ILLEGAL_CONDITION);
        }

        List<BookLocationRel> relations = bookLocationRelRepository.findByBookId(bookBindingDto.getBookId());
        BookLocationRel relation = checkRelationIdentical(relations, bookBindingDto);
        log.info("[BindBookToLocation] request:{}, found book {}, found locations{}", bookBindingDto, book, relations);

        if (Boolean.FALSE.equals(bookBindingDto.getBinding())) {
            // 处理解绑操作
            // 如果解绑时请求的数量大于在位置上的数量，不合法
            if (bookBindingDto.getPlaceAmount() > relation.getReplicationAmount()) {
                throw ApiException.of(FailedReason.LOCATION_HAS_NO_BOOK);
            }
            // 减去操作数量
            relation.setReplicationAmount(relation.getReplicationAmount() - bookBindingDto.getPlaceAmount());
            // 解绑后图书可借库存减少
            book.setAvailableReplicationAmount(
                    book.getAvailableReplicationAmount() - bookBindingDto.getPlaceAmount());

            // 记录下架行为
            historyService.logAdminUserTakeOffBook(book.getId(), DataUtil.getCurrentUserId());
        } else {
            // 处理绑定操作
            // 如果绑定时请求的数量大于非在位图书，不合法
            if (bookBindingDto.getPlaceAmount() > replicationAmountCanBeBind) {
                throw ApiException.of(FailedReason.SUFFICIENT_BOOK_FOR_PLACE);
            }
            // 加上操作数量
            relation.setReplicationAmount(relation.getReplicationAmount() + bookBindingDto.getPlaceAmount());

            // 绑定后图书可借库存增加
            book.setAvailableReplicationAmount(
                    book.getAvailableReplicationAmount() + bookBindingDto.getPlaceAmount());

            // 上架后发起订阅满足通知
            subscriptionService.subscriptionFulfilled(relation);
            // 记录上架行为
            historyService.logAdminUserPutOnBook(book.getId(), DataUtil.getCurrentUserId());
        }

        bookLocationRelRepository.save(relation);
        bookRepository.save(book);
    }

    /**
     * 上架/下架图书
     */
    public void bindBookToLocation(LocationDto locationDto, Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> ApiException.of(FailedReason.BOOK_NOT_EXISTS));
        int replicationAmountCanBeBind = book.getTotalReplicationAmount() - book.getAvailableReplicationAmount();

        if (replicationAmountCanBeBind < 0) {
            throw ApiException.of(FailedReason.ILLEGAL_CONDITION);
        }

        List<BookLocationRel> relations = bookLocationRelRepository.findByBookId(bookId);
        BookLocationRel relation = checkRelationIdentical(relations, locationDto.getId(), bookId);
        log.info("[BindBookToLocation] request:{}, found book {}, found locations{}", locationDto, book, relations);

        // 如果书架上的书 数量小于 想要设置的数量，则是解绑
        boolean isBinding = relation.getReplicationAmount() < locationDto.getReplicationNumber();

        if (Boolean.FALSE.equals(isBinding)) {
            int deductAmount = relation.getReplicationAmount() - locationDto.getReplicationNumber();
            // 处理解绑操作
            // 如果解绑时请求的数量大于在位置上的数量，不合法
            if (deductAmount > relation.getReplicationAmount()) {
                throw ApiException.of(FailedReason.LOCATION_HAS_NO_BOOK);
            }
            // 减去操作数量
            relation.setReplicationAmount(relation.getReplicationAmount() - deductAmount);
            // 解绑后图书可借库存减少
            book.setAvailableReplicationAmount(
                    book.getAvailableReplicationAmount() - deductAmount);

            // 记录下架行为
            historyService.logAdminUserTakeOffBook(book.getId(), DataUtil.getCurrentUserId());
        } else {
            int incrementAmount = locationDto.getReplicationNumber() - relation.getReplicationAmount();
            // 处理绑定操作
            // 如果绑定时请求的数量大于非在位图书，不合法
            if (incrementAmount > replicationAmountCanBeBind) {
                throw ApiException.of(FailedReason.SUFFICIENT_BOOK_FOR_PLACE);
            }
            // 加上操作数量
            relation.setReplicationAmount(relation.getReplicationAmount() + incrementAmount);

            // 绑定后图书可借库存增加
            book.setAvailableReplicationAmount(
                    book.getAvailableReplicationAmount() + incrementAmount);

            // 上架后发起订阅满足通知
            subscriptionService.subscriptionFulfilled(relation);
            // 记录上架行为
            historyService.logAdminUserPutOnBook(book.getId(), DataUtil.getCurrentUserId());
        }

        bookLocationRelRepository.save(relation);
        bookRepository.save(book);
        bookLocationRelRepository.clearEmptyRel();
    }

    /**
     * 检查请求中要上架的位置是否已存在，以及检查一个楼能只能上架一处
     */
    private BookLocationRel checkRelationIdentical(List<BookLocationRel> relations, BookBindingDto bookBindingDto) {
        // 如果上架位置已存在，继续使用位置
        for (BookLocationRel relation : relations) {
            if (relation.getLocationId().equals(bookBindingDto.getLocationId())) {
                return relation;
            }
        }
        // 否则检查新上架位置是否是是同一个楼
        List<Long> locationIds = relations.stream().map(BookLocationRel::getLocationId).collect(Collectors.toList());
        locationIds.add(bookBindingDto.getLocationId());
        locationRepository.findAllByIdIn(locationIds)
                .stream()
                .collect(Collectors.groupingBy(Location::getBuildingName))
                .forEach((buildingName, locations) -> {
                    if (locations.size() > 1) {
                        throw ApiException.of(FailedReason.LOCATION_NOT_ALLOWED);
                    }
                });
        return BookLocationRel.builder()
                .locationId(bookBindingDto.getLocationId())
                .bookId(bookBindingDto.getBookId())
                .replicationAmount(0)
                .build();
    }

    /**
     * 检查请求中要上架的位置是否已存在，以及检查一个楼能只能上架一处
     */
    private BookLocationRel checkRelationIdentical(List<BookLocationRel> relations, Long locationId, Long bookId) {
        // 如果上架位置已存在，继续使用位置
        for (BookLocationRel relation : relations) {
            if (relation.getLocationId().equals(locationId)) {
                return relation;
            }
        }
        // 否则检查新上架位置是否是是同一个楼
        List<Long> locationIds = relations.stream().map(BookLocationRel::getLocationId).collect(Collectors.toList());
        locationIds.add(locationId);
        locationRepository.findAllByIdIn(locationIds)
                .stream()
                .collect(Collectors.groupingBy(Location::getBuildingName))
                .forEach((buildingName, locations) -> {
                    if (locations.size() > 1) {
                        throw ApiException.of(FailedReason.LOCATION_NOT_ALLOWED);
                    }
                });
        return BookLocationRel.builder()
                .locationId(locationId)
                .bookId(bookId)
                .replicationAmount(0)
                .build();
    }
}
