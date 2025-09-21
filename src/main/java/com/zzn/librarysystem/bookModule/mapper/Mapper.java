package com.zzn.librarysystem.bookModule.mapper;

import com.zzn.librarysystem.bookModule.domain.Book;
import com.zzn.librarysystem.bookModule.domain.Location;
import com.zzn.librarysystem.bookModule.domain.Notification;
import com.zzn.librarysystem.bookModule.dto.BookDetailInfoDto;
import com.zzn.librarysystem.bookModule.dto.BookInfoDto;
import com.zzn.librarysystem.bookModule.dto.LocationDto;
import com.zzn.librarysystem.bookModule.dto.NotificationDto;
import com.zzn.librarysystem.bookModule.imports.BookImportModel;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 对象转换器
 */
@Component
public class Mapper extends ConfigurableMapper {
    @Override
    protected void configure(MapperFactory factory) {
        factory.classMap(Book.class, BookInfoDto.class)
                .byDefault()
                .register();
        factory.classMap(Book.class, BookDetailInfoDto.class)
                .byDefault()
                .register();
        factory.classMap(Location.class, LocationDto.class)
                .byDefault()
                .register();
        factory.classMap(Notification.class, NotificationDto.class)
                .byDefault()
                .register();
        factory.classMap(BookImportModel.class, Book.class)
                .byDefault()
                .register();

        factory.getConverterFactory().registerConverter(new InstantConvertor());
        factory.getConverterFactory().registerConverter(new InstantPassThroughConvertor());
    }

    private static class InstantConvertor extends BidirectionalConverter<Instant, String> {

        @Override
        public String convertTo(Instant instant, Type<String> type) {
            return instant.toString();
        }

        @Override
        public Instant convertFrom(String s, Type<Instant> type) {
            return Instant.parse(s);
        }
    }

    private static class InstantPassThroughConvertor extends BidirectionalConverter<Instant, Instant> {

        @Override
        public Instant convertTo(Instant instant, Type<Instant> type) {
            return instant;
        }

        @Override
        public Instant convertFrom(Instant instant, Type<Instant> type) {
            return instant;
        }
    }
}
