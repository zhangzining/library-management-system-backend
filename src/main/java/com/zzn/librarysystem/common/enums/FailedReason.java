package com.zzn.librarysystem.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum FailedReason {
    // 鉴权相关
    CLIENT_ID_NOT_FOUND("CLIENT_ID_NOT_FOUND", "缺少客户端ID"),
    AUTH_FAILED("AUTH_FAILED"),
    REQUEST_LOGIN_FAILED("REQUEST_LOGIN_FAILED", "认证失败", HttpStatus.INTERNAL_SERVER_ERROR),
    WRONG_PASSWORD("WRONG_PASSWORD", "密码错误"),

    // 用户相关
    USERNAME_EXISTS("USERNAME_EXISTS", "该用户已存在"),
    USERNAME_NOT_EXISTS("USERNAME_NOT_EXISTS", "该用户不存在"),
    USERNAME_NOT_VALID("USERNAME_NOT_VALID", "内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_LOCKED("USERNAME_LOCKED", "该账户已锁定"),

    // 图书相关
    BOOK_NOT_EXISTS("BOOK_NOT_EXISTS", "图书不存在", HttpStatus.NOT_FOUND),

    // 文件相关
    UNZIP_FILE_FAILED("UNZIP_FILE_FAILED", "解压失败", HttpStatus.INTERNAL_SERVER_ERROR),
    IMPORT_FAILED("IMPORT_FAILED", "导入失败", HttpStatus.INTERNAL_SERVER_ERROR),
    WRONG_IMPORT_FILE("WRONG_IMPORT_FILE", "上传的文件非ZIP格式"),
    GET_FILE_TYPE_FAILED("IMPORT_FILE_FAILED", "获取图书类型失败", HttpStatus.INTERNAL_SERVER_ERROR),
    SAVE_FILE_FAILED("SAVE_FILE_FAILED", "保存文件失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TYPE_NOT_SUPPORT("FILE_TYPE_NOT_SUPPORT", "不支持的文件类型"),

    // 操作相关
    LOCATION_HAS_NO_BOOK("LOCATION_HAS_NO_BOOK", "当前图书不在该位置"),
    LOCATION_NOT_ALLOWED("LOCATION_NOT_ALLOWED", "不允许上架同一楼栋"),
    SUFFICIENT_BOOK_FOR_PLACE("SUFFICIENT_BOOK_FOR_PLACE", "图书数量不足"),
    ILLEGAL_CONDITION("ILLEGAL_CONDITION", "内部错误", HttpStatus.INTERNAL_SERVER_ERROR),

    // 借阅相关
    SUFFICIENT_BOOK_FOR_LENDING("SUFFICIENT_BOOK_FOR_LENDING", "图书数量不足"),
    BOOK_NOT_IN_LOCATION("BOOK_NOT_IN_LOCATION", "图书未上架"),
    BORROWING_RECORD_NOT_EXISTS("BORROWING_RECORD_NOT_EXISTS", "借阅记录不存在"),
    HAD_BORROWED_SAME_BOOK("HAD_BORROWED_SAME_BOOK", "不允许重复借阅"),


    // 通知相关
    NOTIFICATION_NOT_EXISTS("NOTIFICATION_NOT_EXISTS", "通知不存在", HttpStatus.NOT_FOUND),
    ;

    private final String failedReason;
    private final String message;
    private final HttpStatusCode httpStatus;

    FailedReason(String failedReason) {
        this.failedReason = failedReason;
        this.message = "请求失败";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    FailedReason(String failedReason, String message) {
        this.failedReason = failedReason;
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    FailedReason(String failedReason, String message, HttpStatusCode httpStatus) {
        this.failedReason = failedReason;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
