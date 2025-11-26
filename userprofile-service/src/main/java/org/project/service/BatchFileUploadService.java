package org.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BatchFileUploadService {
    
    /**
     * Upload nhiều file song song
     * @param files Danh sách file cần upload
     * @param userId ID của người dùng
     * @return Map với key là tên file gốc và value là URL đã upload
     */
    CompletableFuture<Map<String, String>> uploadMultipleFiles(List<MultipartFile> files, UUID userId);
    
    /**
     * Upload một file bất đồng bộ
     * @param file File cần upload
     * @param userId ID của người dùng
     * @return CompletableFuture chứa URL của file
     */
    CompletableFuture<String> uploadFileAsync(MultipartFile file, UUID userId);
    
    /**
     * Xóa nhiều file song song
     * @param fileUrls Danh sách URL file cần xóa
     * @return CompletableFuture hoàn thành khi tất cả file đã xóa
     */
    CompletableFuture<Void> deleteMultipleFilesAsync(List<String> fileUrls);
}
