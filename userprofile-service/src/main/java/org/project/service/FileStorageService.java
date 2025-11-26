package org.project.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FileStorageService {

    String uploadUserPhoto(MultipartFile file, UUID userId);

    CompletableFuture<String> uploadUserPhotoAsync(MultipartFile file, UUID userId);

    /**
     * @param avatarUrl Đường dẫn ảnh cũ cần xóa
     */
    void deleteOldPhoto(String avatarUrl);

    /**
     * Xóa ảnh cũ bất đồng bộ
     * @param avatarUrl Đường dẫn ảnh cũ cần xóa
     */
    CompletableFuture<Void> deleteOldPhotoAsync(String avatarUrl);

    /**
     * @param file File cần kiểm tra
     * @return true nếu file hợp lệ
     */
    boolean isValidImageFile(MultipartFile file);
}
