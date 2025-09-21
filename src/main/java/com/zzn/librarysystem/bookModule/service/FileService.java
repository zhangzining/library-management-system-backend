package com.zzn.librarysystem.bookModule.service;

import cn.hutool.core.io.FileTypeUtil;
import com.zzn.librarysystem.bookModule.domain.LocalFile;
import com.zzn.librarysystem.bookModule.dto.BookImportResultDto;
import com.zzn.librarysystem.bookModule.repository.LocalFileRepository;
import com.zzn.librarysystem.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.zzn.librarysystem.common.enums.FailedReason.*;
import static com.zzn.librarysystem.common.util.DataUtil.newUUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {
    @Value("${book.import.image_storage_dir}")
    private String imageStorageDir;

    private final BookImportService bookImportService;
    private final LocalFileRepository localFileRepository;

    private static final String ZIP_FILE_TYPE = "zip";
    private static final Map<String, String> IMAGE_FILE_TYPES_MAP = Map.of(
            "jpg", MediaType.IMAGE_JPEG_VALUE,
            "jpeg", MediaType.IMAGE_JPEG_VALUE,
            "png", MediaType.IMAGE_PNG_VALUE,
            "gif", MediaType.IMAGE_GIF_VALUE);
    private static final List<String> IMPORT_EXCEL_TYPES = List.of("xls", "xlsx");

    public List<BookImportResultDto> importBooksByZipFile(MultipartFile uploadedZipFile) {
        // 1.检查文件类型
        String fileType = null;
        try {
            fileType = getFileType(uploadedZipFile);
        } catch (IOException e) {
            throw ApiException.of(GET_FILE_TYPE_FAILED);
        }
        // 2.检查是否是ZIP文件
        if (!ZIP_FILE_TYPE.equalsIgnoreCase(fileType)) {
            throw ApiException.of(WRONG_IMPORT_FILE);
        }
        // 3.导入文件
        try {
            return unzipFileAndImport(uploadedZipFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw ApiException.of(UNZIP_FILE_FAILED);
        }
    }

    private List<BookImportResultDto> unzipFileAndImport(MultipartFile uploadedZipFile) throws IOException {
        // 2.建立保存图片文件夹
        Path imageUploadFolder = Path.of(imageStorageDir);

        return unzip(uploadedZipFile.getInputStream(), imageUploadFolder);
    }

    public String getFileType(MultipartFile file) throws IOException {
        return Optional.ofNullable(FileTypeUtil.getType(file.getInputStream()))
                .orElseThrow(() -> ApiException.of(GET_FILE_TYPE_FAILED));
    }

    /**
     * 解压文件内容
     */
    public List<BookImportResultDto> unzip(InputStream is, Path imageUploadFolder) throws IOException {
        Path tempDirectory = Files.createTempDirectory("bookSystem");
        List<BookImportResultDto> resultDtos = new ArrayList<>();

        try (ZipInputStream zipIn = new ZipInputStream(is, Charset.forName("GBK"))) {
            List<Path> excelFiles = new ArrayList<>();
            List<Path> imageFiles = new ArrayList<>();

            // 遍历Zip内所有元素
            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
                if (ze.isDirectory()) {
                    log.debug("[Unzip] found directory: {}", ze.getName());
                } else {
                    log.debug("[Unzip] found file: {}", ze.getName());
                    String fileName = resolveFileName(ze.getName());
                    Optional<String> fileExtension = resolveFileExtension(fileName);
                    if (fileExtension.isEmpty()) {
                        continue;
                    }
                    // 复制图片到图片目录
                    if (IMAGE_FILE_TYPES_MAP.containsKey(fileExtension.get())) {
                        Path targetImagePath = imageUploadFolder.resolve(fileName).normalize();
                        Files.createDirectories(targetImagePath.getParent());
                        Files.copy(zipIn, targetImagePath, StandardCopyOption.REPLACE_EXISTING);
                        imageFiles.add(targetImagePath);
                    }
                    // 保存Excel临时文件
                    if (IMPORT_EXCEL_TYPES.contains(fileExtension.get())) {
                        Path temporaryExcelFile = tempDirectory.resolve(ze.getName()).normalize();
                        Files.createDirectories(temporaryExcelFile.getParent());
                        Files.copy(zipIn, temporaryExcelFile, StandardCopyOption.REPLACE_EXISTING);
                        excelFiles.add(temporaryExcelFile);
                    }
                }
            }

            Map<String, String> imageNameUidMap = new HashMap<>();
            // 导入图片
            for (Path imageFile : imageFiles) {
                String filename = imageFile.getFileName().toString();
                String fileType = resolveFileExtension(filename).orElse("");
                Supplier<InputStream> inputStreamSupplier = () -> {
                    try {
                        return Files.newInputStream(imageFile);
                    } catch (IOException e) {
                        throw ApiException.of(SAVE_FILE_FAILED);
                    }
                };
                String fileUid = saveFile(inputStreamSupplier, filename, fileType);
                imageNameUidMap.put(resolvePureFileName(filename), fileUid);
            }

            for (Path excelFile : excelFiles) {
                resultDtos.add(bookImportService.importBookFromExcel(excelFile, imageNameUidMap));
            }
        }

        return resultDtos;
    }

    private Optional<String> resolveFileExtension(String fileName) {
        return Optional.of(fileName)
                .map(name -> name.substring(name.lastIndexOf(".") + 1))
                .map(String::toLowerCase);
    }

    private String resolveFileName(String filePathName) {
        String[] pathElements = filePathName.split("/");
        if (pathElements.length > 1) {
            return pathElements[pathElements.length - 1];
        }
        return filePathName;
    }

    private String resolvePureFileName(String filename) {
        String[] pathElements = filename.split("\\.");
        if (pathElements.length > 1) {
            return pathElements[0];
        }
        return filename;
    }

    public Optional<LocalFile> getLocalFileByUid(String uid) {
        return localFileRepository.findByUid(uid);
    }

    public Path getLocalFilePath(String fileName) {
        return Path.of(imageStorageDir).resolve(fileName);
    }

    /**
     * 保存文件并返回文件UID
     */
    public String saveFile(MultipartFile inputFile) throws IOException {
        String fileName = inputFile.getOriginalFilename();
        String fileType = getFileType(inputFile);
        Supplier<InputStream> inputStreamSupplier = () -> {
            try {
                return inputFile.getInputStream();
            } catch (IOException e) {
                throw ApiException.of(SAVE_FILE_FAILED);
            }
        };
        return saveFile(inputStreamSupplier, fileName, fileType);
    }

    /**
     * 保存文件并返回文件UID
     */
    public String saveFile(Supplier<InputStream> inputStreamSupplier, String filename, String fileType) throws IOException {
        String md5 = DigestUtils.md5DigestAsHex(inputStreamSupplier.get());
        Optional<LocalFile> fileOptional = localFileRepository.findByHash(md5);

        // 如果找到相同HASH值的文件，直接使用该文件的UID
        if (fileOptional.isPresent()) {
            log.debug("[SaveFile] found same file: {}", fileOptional.get().getName());
            return fileOptional.get().getUid();
        }

        // 如果没有找到相同HASH值的文件，保存该文件
        if (!IMAGE_FILE_TYPES_MAP.containsKey(fileType)) {
            log.error("[SaveFile] File type:{} not supported with name {}", fileType, filename);
            throw ApiException.of(FILE_TYPE_NOT_SUPPORT);
        }
        String mediaType = IMAGE_FILE_TYPES_MAP.get(fileType);

        Path destFilePath = getLocalFilePath(filename);
        log.debug("[SaveFile] Filename:{}, FileType:{}, destFilePath:{}", filename, fileType, destFilePath.toAbsolutePath());

        // 复制文件
        Files.createDirectories(destFilePath.getParent());
        Files.copy(inputStreamSupplier.get(), destFilePath, StandardCopyOption.REPLACE_EXISTING);
        // 保存记录
        LocalFile localFile = LocalFile.builder()
                .name(filename)
                .uid(newUUID())
                .hash(md5)
                .mediaType(mediaType)
                .build();
        localFileRepository.save(localFile);
        return localFile.getUid();
    }
}
