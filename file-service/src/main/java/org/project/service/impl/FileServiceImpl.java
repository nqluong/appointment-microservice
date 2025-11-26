package org.project.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.config.MinioConfig;
import org.project.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {
    MinioClient minioClient;

    MinioConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        createBucketIfNotExists();

        String fileName = generateFileName(file.getOriginalFilename());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("Tải lên tệp thành công: {}", fileName);
        return fileName;
    }

    @Override
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .build()
        );
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .build()
        );
        log.info("Xóa tệp thành công: {}", fileName);
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .expiry(60 * 60 * 24)
                            .build()
            );
        } catch (Exception e) {
            log.error("Lỗi tạo URL ký trước cho tệp: {}", fileName, e);
            return null;
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
        );

        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            log.info("Tạo bucket thành công: {}", minioConfig.getBucketName());
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            extension = originalFilename.substring(lastDotIndex).trim().toLowerCase();
            // Đảm bảo extension không chứa khoảng trắng hoặc ký tự đặc biệt
            extension = extension.replaceAll("[^a-z0-9.]", "");
        }
        return UUID.randomUUID().toString() + extension;
    }
}
