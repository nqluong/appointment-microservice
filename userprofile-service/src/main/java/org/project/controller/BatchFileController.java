package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.common.security.annotation.RequireOwnershipOrAdmin;
import org.project.dto.ApiResponse;
import org.project.dto.ErrorResponse;
import org.project.service.BatchFileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users/{userId}/files")
@RequiredArgsConstructor
@Slf4j
public class BatchFileController {

    private final BatchFileUploadService batchFileUploadService;

    @PostMapping(value = "/batch-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
    public CompletableFuture<ResponseEntity<?>> batchUploadFiles(
            @PathVariable UUID userId,
            @RequestParam("files") MultipartFile[] files) {

        try {
            if (files == null || files.length == 0) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                        ErrorResponse.builder()
                            .success(false)
                            .code(400)
                            .message("Vui lòng chọn ít nhất một file để tải lên")
                            .timestamp(LocalDateTime.now())
                            .build()
                    )
                );
            }

            if (files.length > 10) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                        ErrorResponse.builder()
                            .success(false)
                            .code(400)
                            .message("Chỉ được phép tải lên tối đa 10 file cùng lúc")
                            .timestamp(LocalDateTime.now())
                            .build()
                    )
                );
            }

            List<MultipartFile> fileList = Arrays.asList(files);

            return batchFileUploadService.uploadMultipleFiles(fileList, userId)
                    .thenApply(results -> {
                        if (results.isEmpty()) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ErrorResponse.builder()
                                    .success(false)
                                    .code(500)
                                    .message("Không thể tải lên bất kỳ file nào")
                                    .timestamp(LocalDateTime.now())
                                    .build()
                            );
                        }

                        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                                .success(true)
                                .code(200)
                                .message(String.format("Tải lên thành công %d/%d file", results.size(), files.length))
                                .data(results)
                                .timestamp(LocalDateTime.now())
                                .build();

                        return ResponseEntity.ok(response);
                    })
                    .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        ErrorResponse.builder()
                            .success(false)
                            .code(500)
                            .message("Lỗi khi tải lên file")
                            .details(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build()
                    ));

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(500)
                        .message("Lỗi không mong đợi khi tải lên file")
                        .details(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
                )
            );
        }
    }

    @PostMapping(value = "/upload-async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
    public CompletableFuture<ResponseEntity<?>> uploadFileAsync(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file == null || file.isEmpty()) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                        ErrorResponse.builder()
                            .success(false)
                            .code(400)
                            .message("File không được để trống")
                            .timestamp(LocalDateTime.now())
                            .build()
                    )
                );
            }

            String originalFileName = file.getOriginalFilename();

            return batchFileUploadService.uploadFileAsync(file, userId)
                    .handle((fileUrl, ex) -> {
                        if (ex != null) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    ErrorResponse.builder()
                                            .success(false)
                                            .code(500)
                                            .message("Lỗi khi tải lên file")
                                            .details(ex.getMessage())
                                            .timestamp(LocalDateTime.now())
                                            .build()
                            );
                        }

                        Map<String, String> data = Map.of(
                                "fileName", originalFileName != null ? originalFileName : "unknown",
                                "fileUrl", fileUrl
                        );

                        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                                .success(true)
                                .code(200)
                                .message("Tải lên file thành công")
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build();

                        return ResponseEntity.ok(response);
                    });

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(500)
                        .message("Lỗi không mong đợi khi tải lên file")
                        .details(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
                )
            );
        }
    }

    @DeleteMapping("/batch-delete")
    @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
    public CompletableFuture<ResponseEntity<?>> batchDeleteFiles(
            @PathVariable UUID userId,
            @RequestBody List<String> fileUrls) {

        try {
            if (fileUrls == null || fileUrls.isEmpty()) {
                log.error("Danh sách file trống cho user {}", userId);
                return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                        ErrorResponse.builder()
                            .success(false)
                            .code(400)
                            .message("Vui lòng cung cấp danh sách file cần xóa")
                            .timestamp(LocalDateTime.now())
                            .build()
                    )
                );
            }

            log.info("Bắt đầu xóa {} file song song cho user {}", fileUrls.size(), userId);

            return batchFileUploadService.deleteMultipleFilesAsync(fileUrls)
                    .handle((v, ex) -> {
                        if (ex != null) {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    ErrorResponse.builder()
                                            .success(false)
                                            .code(500)
                                            .message("Lỗi khi xóa file")
                                            .details(ex.getMessage())
                                            .timestamp(LocalDateTime.now())
                                            .build()
                            );
                        }

                        ApiResponse<String> response = ApiResponse.<String>builder()
                                .success(true)
                                .code(200)
                                .message(String.format("Đã xóa %d file thành công", fileUrls.size()))
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .build();

                        return ResponseEntity.ok(response);
                    });

        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi xóa file cho user {}: {}", userId, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(500)
                        .message("Lỗi không mong đợi khi xóa file")
                        .details(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
                )
            );
        }
    }
}