package org.project.service;

import java.util.List;
import java.util.Map;

public interface AvatarUrlService {
    /**
     * Tạo presigned URL từ tên file
     * @param fileName Tên file trong MinIO
     * @return Presigned URL hoặc null nếu không tạo được
     */
    String generatePresignedUrl(String fileName);

    /**
     * Tạo presigned URLs cho nhiều file cùng lúc
     * @param fileNames Danh sách tên file
     * @return Map với key là tên file và value là presigned URL
     */
    Map<String, String> generateBatchPresignedUrls(List<String> fileNames);
}
