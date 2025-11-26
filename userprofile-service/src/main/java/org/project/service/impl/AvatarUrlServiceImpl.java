package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.FileServiceClient;
import org.project.dto.ApiResponse;
import org.project.service.AvatarUrlService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AvatarUrlServiceImpl implements AvatarUrlService {

    FileServiceClient fileServiceClient;

    @Override
    public String generatePresignedUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        try {
            ApiResponse<Map<String, String>> response = fileServiceClient.getFileUrl(fileName);
            
            if (response.isSuccess() && response.getData() != null) {
                return response.getData().get("fileUrl");
            }
            
            log.warn("Không thể tạo presigned URL cho file: {}", fileName);
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tạo presigned URL cho file {}: {}", fileName, e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, String> generateBatchPresignedUrls(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> validFileNames = fileNames.stream()
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (validFileNames.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            log.debug("Đang tạo presigned URLs cho {} file", validFileNames.size());
            ApiResponse<Map<String, String>> response = fileServiceClient.getBatchFileUrls(validFileNames);
            
            if (response.isSuccess() && response.getData() != null) {
                log.info("Tạo thành công presigned URLs cho {}/{} file", 
                    response.getData().size(), validFileNames.size());
                return response.getData();
            }
            
            log.warn("Không thể tạo batch presigned URLs");
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Lỗi khi tạo batch presigned URLs: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
