package com.zzn.librarysystem.common.exception;

import com.zzn.librarysystem.common.enums.FailedReason;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatusCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
public class ApiException extends RuntimeException{
    String failedReason;
    String message;
    HttpStatusCode httpStatus;
    String rawJson;


    public ApiException() {
        super();
    }

    public ApiException(String failedReason, String message, HttpStatusCode statusCode) {
        this.failedReason = failedReason;
        this.message = message;
        this.httpStatus = statusCode;
    }

    public ApiException(String failedReason) {
        this.failedReason = failedReason;
    }

    public boolean hadRawJson() {
        return StringUtils.isNotBlank(rawJson);
    }

    public static ApiException of(FailedReason failedReason) {
        return ApiException.builder()
                .httpStatus(failedReason.getHttpStatus())
                .failedReason(failedReason.getFailedReason())
                .message(failedReason.getMessage())
                .build();
    }
}
