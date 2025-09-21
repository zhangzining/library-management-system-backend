package com.zzn.librarysystem.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDto {
    String failedReason;
    String message;
    Map<String, Object> param;
}
