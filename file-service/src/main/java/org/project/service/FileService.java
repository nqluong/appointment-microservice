package org.project.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.config.MinioConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


public interface FileService {


    public String uploadFile(MultipartFile file) throws Exception;

    public InputStream downloadFile(String fileName) throws Exception;

    public void deleteFile(String fileName) throws Exception;

    public String getFileUrl(String fileName) throws Exception;

}