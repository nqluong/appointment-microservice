package org.project.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileValidator {
    void validate(MultipartFile file);
    boolean isValid(MultipartFile file);
}
