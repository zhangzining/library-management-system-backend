package com.zzn.librarysystem.common.util;

import com.zzn.librarysystem.authServer.dto.SimpleUserInfo;
import com.zzn.librarysystem.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zzn.librarysystem.common.enums.FailedReason.CLIENT_ID_NOT_FOUND;

public class DataUtil {
    public static final String HEADER_CLIENT_ID = "X-Client-ID";
    private static final String NAME_PREFIX = "借阅用户_";
    private static final Random RANDOM = new Random();

    private DataUtil(){}
    /**
     * 强制要求获取 clientId 不然报错
     */
    public static String getMandatoryClientId() {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        return Optional.ofNullable(request.getHeader(HEADER_CLIENT_ID))
                .orElseThrow(() -> ApiException.of(CLIENT_ID_NOT_FOUND));
    }

    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken authenticationToken) {
            return authenticationToken.getName();
        }
        return authentication.getName();
    }

    public static String randomName() {
        int id = randomNum(6);
        return String.format("%s%06d", NAME_PREFIX, id);
    }

    public static Integer randomNum(int digit) {
        return RANDOM.nextInt((int) Math.pow(10, digit-1));
    }

    public static String newUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void applyIfNotBlank(Supplier<String> getter, Consumer<String> setter) {
        Optional.ofNullable(getter.get()).filter(StringUtils::isNotBlank).ifPresent(setter);
    }

    public static <T> void applyIfNotNull(Supplier<T> getter, Consumer<T> setter) {
        Optional.ofNullable(getter.get()).ifPresent(setter);
    }

    /**
     * 从 SecurityContext 中获取用户ID
     */
    public static Long getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication instanceof JwtAuthenticationToken) {
                SimpleUserInfo userInfo = UsernameUtil.getUserInfoByUsername(authentication.getName());
                return userInfo.getId();
            }
        }
        return null;
    }

    /**
     * 从 SecurityContext 中获取用户名称
     */
    public static String getCurrentUserName() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication instanceof JwtAuthenticationToken) {
                SimpleUserInfo userInfo = UsernameUtil.getUserInfoByUsername(authentication.getName());
                return userInfo.getUsername();
            }
        }
        return null;
    }


    public static SimpleUserInfo getCurrentUserInfo() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication instanceof JwtAuthenticationToken) {
                return UsernameUtil.getUserInfoByUsername(authentication.getName());
            }
        }
        return null;
    }

    public static Instant localDateToInstant(LocalDate localDate) {
        // 指定时间为午夜（00:00:00）
        LocalDateTime localDateTime = localDate.atStartOfDay();

        // 指定时区
        ZoneId zoneId = ZoneId.systemDefault();

        // 将 LocalDateTime 转换为 ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        // 将 ZonedDateTime 转换为 Instant
        return zonedDateTime.toInstant();
    }

    public static LocalDate instantToLocalDate(Instant instant) {
        // 获取系统默认时区
        ZoneId zoneId = ZoneId.systemDefault();

        // 将 Instant 转换为 LocalDate
        return instant.atZone(zoneId).toLocalDate();
    }

    public static <T> List<T> randomSelect(List<T> list, int count) {
        if (list.size() <= count) {
            return new ArrayList<>(list);
        }
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled, new Random());
        return shuffled.subList(0, count);
    }
}
