package org.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    /**
     * @param file File ảnh cần lưu
     * @param userId ID của người dùng
     * @return Đường dẫn tương đối của file đã lưu
     */
    String saveUserPhoto(MultipartFile file, UUID userId);

    /**
     * @param avatarUrl Đường dẫn ảnh cũ cần xóa
     */
    void deleteOldPhoto(String avatarUrl);

    /**
     * @param file File cần kiểm tra
     * @return true nếu file hợp lệ
     */
    boolean isValidImageFile(MultipartFile file);
}
