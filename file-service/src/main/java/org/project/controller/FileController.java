package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.ApiResponse;
import org.project.dto.ErrorResponse;
import org.project.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                log.error("Tệp trống được gửi lên");
                return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(400)
                        .message("Tệp không được để trống")
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            }

            if (!isImageFile(file)) {
                log.error("Loại tệp không hợp lệ: {}", file.getContentType());
                return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(400)
                        .message("Chỉ cho phép tải lên tệp hình ảnh")
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            }

            String fileName = fileService.uploadFile(file);
            String fileUrl = fileService.getFileUrl(fileName);

            Map<String, String> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("fileUrl", fileUrl);

            ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .code(200)
                    .message("Tải lên tệp thành công")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tải lên tệp: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                    .success(false)
                    .code(500)
                    .message("Không thể tải lên tệp")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        try {
            InputStream fileStream = fileService.downloadFile(fileName);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(fileStream));
        } catch (Exception e) {
            log.error("Lỗi khi tải xuống tệp: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        try {
            fileService.deleteFile(fileName);
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .code(200)
                    .message("Xóa tệp thành công")
                    .data(fileName)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xóa tệp: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                    .success(false)
                    .code(500)
                    .message("Không thể xóa tệp")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    @GetMapping("/url/{fileName}")
    public ResponseEntity<?> getFileUrl(@PathVariable String fileName) {
        try {
            String fileUrl = fileService.getFileUrl(fileName);
            if (fileUrl != null) {
                Map<String, String> data = Map.of("fileUrl", fileUrl);
                
                ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .code(200)
                        .message("Lấy URL tệp thành công")
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build();
                        
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(404)
                        .message("Không tìm thấy tệp")
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy URL tệp: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                    .success(false)
                    .code(500)
                    .message("Không thể lấy URL tệp")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    @PostMapping("/batch-urls")
    public ResponseEntity<?> getBatchFileUrls(@RequestBody java.util.List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(400)
                        .message("Danh sách tên file không được để trống")
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            }

            Map<String, String> urlMap = new HashMap<>();
            
            for (String fileName : fileNames) {
                if (fileName != null && !fileName.isEmpty()) {
                    String fileUrl = fileService.getFileUrl(fileName);
                    if (fileUrl != null) {
                        urlMap.put(fileName, fileUrl);
                    }
                }
            }

            ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .code(200)
                    .message(String.format("Lấy URL thành công cho %d/%d file", urlMap.size(), fileNames.size()))
                    .data(urlMap)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy batch URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                    .success(false)
                    .code(500)
                    .message("Không thể lấy URL cho các file")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}