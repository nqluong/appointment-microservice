package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.PhotoUploadRequest;
import org.project.dto.response.PhotoUploadResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.repository.UserProfileRepository;
import org.project.service.FileStorageService;
import org.project.service.PhotoUploadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhotoUploadServiceImpl implements PhotoUploadService {
//    FileStorageService fileStorageService;
//    UserProfileRepository userProfileRepository;
//
//    @Override
//    @Transactional
//    public PhotoUploadResponse uploadUserPhoto(UUID userId, PhotoUploadRequest request) {
//        try {
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//
//            UserProfile userProfile = getUserProfileOrCreate(user);
//            String oldAvatarUrl = userProfile.getAvatarUrl();
//
//            String newAvatarUrl = fileStorageService.saveUserPhoto(request.getPhoto(), userId);
//
//            // Cập nhật đường dẫn ảnh
//            userProfile.setAvatarUrl(newAvatarUrl);
//            userProfileRepository.save(userProfile);
//
//            // Xóa ảnh cũ nếu có
//            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
//                fileStorageService.deleteOldPhoto(oldAvatarUrl);
//            }
//
//            log.info("Upload ảnh thành công cho user ID: {}", userId);
//
//            return PhotoUploadResponse.builder()
//                    .success(true)
//                    .avatarUrl(newAvatarUrl)
//                    .message("Photo uploaded successfully")
//                    .build();
//
//        } catch (CustomException e) {
//            // Re-throw CustomException để GlobalExceptionHandler xử lý
//            log.error("Failed to upload photo for user {}: {}", userId, e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.PHOTO_UPLOAD_ERROR);
//        }
//    }
//
//    private UserProfile getUserProfileOrCreate(User user) {
//        UserProfile userProfile = user.getUserProfile();
//
//        if (userProfile == null) {
//            userProfile = UserProfile.builder()
//                    .user(user)
//                    .firstName("")
//                    .lastName("")
//                    .build();
//            userProfile = userProfileRepository.save(userProfile);
//        }
//        return userProfile;
//    }
}
