package org.project.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.FileValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Slf4j
public class ImageFileValidator implements FileValidator {
    @Value("${app.upload.max-size:5242880}")
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif"
    );

    @Override
    public void validate(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file);
        validateContentType(file);
        validateExtension(file);
    }

    @Override
    public boolean isValid(MultipartFile file) {
        try {
            validate(file);
            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File is null or empty");
            throw new CustomException(ErrorCode.FILE_NOT_PROVIDED);
        }
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            log.warn("File size exceeded. Size: {} bytes, Max: {} bytes",
                    file.getSize(), maxFileSize);
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("Invalid content type: {}", contentType);
            throw new CustomException(ErrorCode.INVALID_FILE_CONTENT_TYPE);
        }
    }

    private void validateExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("Original filename is null");
            throw new CustomException(ErrorCode.FILE_NOT_PROVIDED);
        }

        String extension = extractExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Invalid file extension: {}", extension);
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
