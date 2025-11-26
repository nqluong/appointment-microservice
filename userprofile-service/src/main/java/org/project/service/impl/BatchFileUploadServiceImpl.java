package org.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.service.BatchFileUploadService;
import org.project.service.FileStorageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchFileUploadServiceImpl implements BatchFileUploadService {

    private final FileStorageService fileStorageService;

    @Override
    @Async("fileUploadExecutor")
    public CompletableFuture<Map<String, String>> uploadMultipleFiles(List<MultipartFile> files, UUID userId) {
        log.info("Bắt đầu upload {} file song song cho user {}", files.size(), userId);
        
        long startTime = System.currentTimeMillis();
        
        // Upload tất cả file song song
        List<CompletableFuture<Map.Entry<String, String>>> uploadFutures = files.stream()
                .map(file -> uploadFileAsync(file, userId)
                        .thenApply(url -> Map.entry(file.getOriginalFilename(), url))
                        .exceptionally(ex -> {
                            log.error("Lỗi upload file {}: {}", file.getOriginalFilename(), ex.getMessage());
                            return Map.entry(file.getOriginalFilename(), null);
                        }))
                .collect(Collectors.toList());

        // Đợi tất cả hoàn thành
        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
                uploadFutures.toArray(new CompletableFuture[0])
        );

        return allUploads.thenApply(v -> {
            Map<String, String> results = new HashMap<>();
            uploadFutures.forEach(future -> {
                try {
                    Map.Entry<String, String> entry = future.get();
                    if (entry.getValue() != null) {
                        results.put(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi lấy kết quả upload: {}", e.getMessage());
                }
            });
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Hoàn thành upload {} file trong {} ms", results.size(), duration);
            
            return results;
        });
    }

    @Override
    @Async("fileUploadExecutor")
    public CompletableFuture<String> uploadFileAsync(MultipartFile file, UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Đang upload file: {} cho user {}", file.getOriginalFilename(), userId);
                String url = fileStorageService.uploadUserPhoto(file, userId);
                log.debug("Upload thành công file: {} -> {}", file.getOriginalFilename(), url);
                return url;
            } catch (Exception e) {
                log.error("Lỗi upload file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("Không thể upload file: " + file.getOriginalFilename(), e);
            }
        });
    }

    @Override
    @Async("fileUploadExecutor")
    public CompletableFuture<Void> deleteMultipleFilesAsync(List<String> fileUrls) {
        log.info("Bắt đầu xóa {} file song song", fileUrls.size());
        
        List<CompletableFuture<Void>> deleteFutures = fileUrls.stream()
                .map(url -> CompletableFuture.runAsync(() -> {
                    try {
                        fileStorageService.deleteOldPhoto(url);
                        log.debug("Xóa thành công file: {}", url);
                    } catch (Exception e) {
                        log.warn("Lỗi khi xóa file {}: {}", url, e.getMessage());
                    }
                }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("Hoàn thành xóa {} file", fileUrls.size()));
    }
}
