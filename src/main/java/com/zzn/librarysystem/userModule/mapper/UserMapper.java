package com.zzn.librarysystem.userModule.mapper;

import com.zzn.librarysystem.userModule.domain.AdminRole;
import com.zzn.librarysystem.userModule.domain.AdminUser;
import com.zzn.librarysystem.userModule.domain.NormalUser;
import com.zzn.librarysystem.userModule.dto.AdminUserDto;
import com.zzn.librarysystem.userModule.dto.NormalUserDto;
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
public class UserMapper extends ConfigurableMapper {
    @Override
    protected void configure(MapperFactory factory) {
        factory.classMap(NormalUser.class, NormalUserDto.class)
                .byDefault()
                .register();

        factory.classMap(AdminUser.class, AdminUserDto.class)
                .byDefault()
                .register();

        factory.getConverterFactory().registerConverter(new AdminRoleConvertor());
        factory.getConverterFactory().registerConverter(new InstantConvertor());
        factory.getConverterFactory().registerConverter(new InstantPassThroughConvertor());
    }

    private static class AdminRoleConvertor extends BidirectionalConverter<AdminRole, String> {

        @Override
        public String convertTo(AdminRole role, Type<String> type) {
            return role.getName();
        }

        @Override
        public AdminRole convertFrom(String s, Type<AdminRole> type) {
            return new AdminRole(s);
        }
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
