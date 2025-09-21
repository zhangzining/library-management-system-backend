package com.zzn.librarysystem.common;

import com.zzn.librarysystem.common.dto.ErrorDto;
import com.zzn.librarysystem.common.exception.ApiException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e) {
        if (e.hadRawJson()) {
            return ResponseEntity.status(e.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getRawJson());
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorDto.builder()
                        .failedReason(e.getFailedReason())
                        .message(e.getMessage())
                        .build());
    }
}
