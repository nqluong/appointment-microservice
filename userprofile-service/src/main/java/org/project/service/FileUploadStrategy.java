package org.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadStrategy {
    String upload(MultipartFile file);
    void delete(String fileName);
}
