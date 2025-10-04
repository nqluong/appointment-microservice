package org.project.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads/avatars}")
    String uploadDir;

    @Value("${app.upload.max-size:5242880}")
    long maxFileSize;

    static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif"
    );

    @Override
    public String saveUserPhoto(MultipartFile file, UUID userId) {
        try {
            validateImageFile(file);
            // Tạo thư mục nếu chưa tồn tại
            createUploadDirectoryIfNotExists();

            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String newFileName = String.format("avatar_%s_%s.%s", userId, timestamp, fileExtension);

            Path uploadPath = Paths.get(uploadDir);
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + "/" + newFileName;

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateImageFile(MultipartFile file) {
        // Kiểm tra file null hoặc empty
        if (file == null || file.isEmpty()) {
            log.warn("File is null or empty");
            throw new CustomException(ErrorCode.FILE_NOT_PROVIDED);
        }

        // Kiểm tra kích thước
        if (file.getSize() > maxFileSize) {
            log.warn("File size exceeded. Size: {} bytes, Max allowed: {} bytes",
                    file.getSize(), maxFileSize);
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // Kiểm tra loại MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("Invalid content type: {}", contentType);
            throw new CustomException(ErrorCode.INVALID_FILE_CONTENT_TYPE);
        }

        // Kiểm tra phần mở rộng
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("Original filename is null");
            throw new CustomException(ErrorCode.FILE_NOT_PROVIDED);
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Invalid file extension: {}", extension);
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    @Override
    public void deleteOldPhoto(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        try {
            // Loại bỏ dấu "/" đầu tiên nếu có
            String cleanPath = avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl;
            Path oldFilePath = Paths.get(cleanPath);

            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
                log.info("Deleted old photo: {}", avatarUrl);
            }
        } catch (IOException e) {
            log.warn("Failed to delete old photos{}: {}", avatarUrl, e.getMessage());
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        try {
            validateImageFile(file);
            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage());
            throw new CustomException(ErrorCode.DIRECTORY_CREATION_FAILED);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
