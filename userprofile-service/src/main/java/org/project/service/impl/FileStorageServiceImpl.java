package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.client.AuthServiceClient;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.UserProfile;
import org.project.repository.UserProfileRepository;
import org.project.service.FileStorageService;
import org.project.service.FileUploadStrategy;
import org.project.service.FileValidator;
import org.project.service.UrlProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageServiceImpl implements FileStorageService {

    FileValidator fileValidator;
    FileUploadStrategy fileUploadStrategy;
    UrlProcessor urlProcessor;
    UserProfileRepository userProfileRepository;
    AuthServiceClient authServiceClient;

    @Transactional
    @Override
    public String uploadUserPhoto(MultipartFile file, UUID userId) {
        try {
            // 1. Validate file
            fileValidator.validate(file);

            // 2. Verify user exists
            authServiceClient.checkExistsbyId(userId);

            // 3. Get user profile
            UserProfile userProfile = userProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            String oldAvatarUrl = userProfile.getAvatarUrl();

            // 4. Upload new photo
            String presignedUrl = fileUploadStrategy.upload(file);
            String fileName = urlProcessor.extractFileName(presignedUrl);

            // 5. Update database
            int updated = userProfileRepository.updateAvatarUrl(userId, fileName);

            if (updated == 0) {
                log.error("Failed to update avatar for user {}", userId);
                throw new CustomException(ErrorCode.UPDATE_FAILED);
            }

            // 6. Cleanup old photo (async, non-blocking)
            if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
                deleteOldPhotoAsync(oldAvatarUrl);
            }

            log.info("Photo updated successfully for user {}: {}", userId, fileName);
            return presignedUrl;

        } catch (CustomException e) {
            log.error("Error updating photo for user {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating photo for user {}: {}",
                    userId, e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public CompletableFuture<String> uploadUserPhotoAsync(MultipartFile file, UUID userId) {
        return CompletableFuture.supplyAsync(() -> uploadUserPhoto(file, userId));
    }

    @Override
    public void deleteOldPhoto(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }

        String fileName = urlProcessor.extractFileName(avatarUrl);
        fileUploadStrategy.delete(fileName);
    }

    @Override
    public CompletableFuture<Void> deleteOldPhotoAsync(String avatarUrl) {
        return CompletableFuture.runAsync(() -> deleteOldPhoto(avatarUrl));
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        return fileValidator.isValid(file);
    }
}
