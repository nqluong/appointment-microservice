package org.project.client;

import org.project.config.FeignConfig;
import org.project.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(name = "file-service", path = "/api/files", configuration = FeignConfig.class)
public interface FileServiceClient {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<Map<String, String>> uploadFile(@RequestPart("file") MultipartFile file);

    @DeleteMapping("/{fileName}")
    ApiResponse<String> deleteFile(@PathVariable("fileName") String fileName);

    @GetMapping("/url/{fileName}")
    ApiResponse<Map<String, String>> getFileUrl(@PathVariable("fileName") String fileName);

    @PostMapping("/batch-urls")
    ApiResponse<Map<String, String>> getBatchFileUrls(@RequestBody java.util.List<String> fileNames);
}
