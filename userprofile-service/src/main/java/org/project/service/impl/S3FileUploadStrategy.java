package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.FileServiceClient;
import org.project.dto.ApiResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.FileUploadStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3FileUploadStrategy implements FileUploadStrategy {

    FileServiceClient fileServiceClient;

    @Override
    public String upload(MultipartFile file) {
        try {
            ApiResponse<Map<String, String>> response = fileServiceClient.uploadFile(file);

            if (response.isSuccess() && response.getData() != null) {
                String fileUrl = response.getData().get("fileUrl");
                log.info("File uploaded successfully: {}", fileUrl);
                return fileUrl;
            }

            log.error("File upload failed: {}", response.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        try {
            ApiResponse<String> response = fileServiceClient.deleteFile(fileName);

            if (response.isSuccess()) {
                log.info("File deleted successfully: {}", fileName);
            } else {
                log.warn("Failed to delete file: {}", fileName);
            }
        } catch (Exception e) {
            log.warn("Error deleting file {}: {}", fileName, e.getMessage());
        }
    }
}
