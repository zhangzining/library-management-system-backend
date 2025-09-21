package com.zzn.librarysystem.bookModule.controller;

import com.zzn.librarysystem.bookModule.domain.LocalFile;
import com.zzn.librarysystem.bookModule.service.FileService;
import com.zzn.librarysystem.common.enums.FailedReason;
import com.zzn.librarysystem.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/files")
@CrossOrigin
public class FileController {
    private final FileService fileService;

    /**
     * 上传图片并返回图片UID
     */
    @PreAuthorize("hasAnyRole('ADMIN_USER') or hasAnyRole('NORMAL_USER')")
    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return fileService.saveFile(file);
        } catch (IOException e) {
            log.error("[UploadFile] save file error", e);
            throw ApiException.of(FailedReason.SAVE_FILE_FAILED);
        }
    }

    /**
     * 通过文件UID获取文件
     */
    @GetMapping("{id}")
    public ResponseEntity<InputStreamResource> getFileContent(@PathVariable("id") String uid) {
        Optional<LocalFile> fileOptional = fileService.getLocalFileByUid(uid);
        if (fileOptional.isPresent()) {
            // 获取图片信息
            LocalFile localFile = fileOptional.get();
            // 获取图片路径
            File imgFile = fileService.getLocalFilePath(localFile.getName()).toFile();

            InputStreamResource resource;
            ResponseEntity<InputStreamResource> response;
            try {
                // 加载图片并返回流
                resource = new InputStreamResource(new FileInputStream(imgFile));
                response = ResponseEntity.ok()
                        .contentType(MediaType.valueOf(localFile.getMediaType()))
                        .body(resource);
            } catch (FileNotFoundException e) {
                log.error("[GetFileContent] Get file failed", e);
                response = ResponseEntity.internalServerError().build();
            }

            return response;
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
