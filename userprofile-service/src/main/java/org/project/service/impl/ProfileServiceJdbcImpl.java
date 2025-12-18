package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.common.security.util.SecurityUtils;
import org.project.dto.request.ProfileUpdateRequest;
import org.project.dto.response.CompleteProfileProjection;
import org.project.dto.response.CompleteProfileResponse;
import org.project.dto.response.UserProfileResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.mapper.ProfileMapper;
import org.project.model.UserProfile;
import org.project.repository.ProfileJdbcRepository;
import org.project.repository.UserProfileRepository;
import org.project.service.AvatarUrlService;
import org.project.service.ProfileService;
import org.project.service.strategy.FieldFilterStrategy;
import org.project.service.strategy.FieldFilterStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceJdbcImpl implements ProfileService {
    UserProfileRepository userProfileRepository;
    ProfileJdbcRepository profileJdbcRepository;
    ProfileMapper profileMapper;
    AvatarUrlService avatarUrlService;
    FieldFilterStrategyFactory fieldFilterStrategyFactory;
    SecurityUtils securityUtils;

    @Override
    @Transactional
    public CompleteProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        try {

            // Lấy danh sách role của người dùng
            Set<String> userRoles = getUserRoles(userId);

            // Lấy theo role cho phù hợp và lọc các trường
            FieldFilterStrategy strategy = fieldFilterStrategyFactory.getStrategy(userRoles);
            ProfileUpdateRequest filteredRequest = strategy.filterFields(request);


            boolean updateSuccess = profileJdbcRepository.updateProfile(userId, filteredRequest);

            if (!updateSuccess) {
                log.warn("Không có trường nào được cập nhật cho userId: {}", userId);
            }

            CompleteProfileResponse response = getCompleteProfileFromRepository(userId);
            
            // Generate presigned URL cho avatar nếu có
            response = enrichWithPresignedAvatarUrl(response);
            
            log.info("Cập nhật profile thành công cho userId: {}", userId);
            return response;

        } catch (CustomException e) {
            log.error("Lỗi khi cập nhật profile cho userId: {} - {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi cập nhật profile cho userId: {}", userId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CompleteProfileResponse getCompleteProfile(UUID userId) {
        try {
            log.info("Lấy complete profile cho userId: {}", userId);

            CompleteProfileResponse response = getCompleteProfileFromRepository(userId);
            
            // Generate presigned URL cho avatar nếu có
            response = enrichWithPresignedAvatarUrl(response);

            return response;

        } catch (CustomException e) {
            log.error("Lỗi khi lấy complete profile cho userId: {} - {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi lấy complete profile cho userId: {}", userId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public UserProfileResponse getUserProfile(UUID userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return profileMapper.toUserProfileResponse(userProfile);
    }

    /**
     * Lấy complete profile từ repository và convert thành response
     */
    private CompleteProfileResponse getCompleteProfileFromRepository(UUID userId) {
        CompleteProfileProjection completeUser = profileJdbcRepository.getCompleteUserProfile(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return profileMapper.toCompleteProfileResponse(completeUser);
    }

    /**
     * Bổ sung presigned URL cho avatar nếu có
     */
    private CompleteProfileResponse enrichWithPresignedAvatarUrl(CompleteProfileResponse response) {
        if (response == null) {
            return null;
        }

        try {
            String avatarFileName = response.getAvatarUrl();
            
            if (avatarFileName != null && !avatarFileName.trim().isEmpty()) {
                String presignedUrl = avatarUrlService.generatePresignedUrl(avatarFileName);
                
                if (presignedUrl != null) {
                    response.setAvatarUrl(presignedUrl);
                } else {
                    log.warn("Không thể generate presigned URL cho avatar: {}", avatarFileName);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi generate presigned URL cho avatar: {}", e.getMessage(), e);
            // Không throw exception, giữ nguyên avatarUrl gốc
        }

        return response;
    }

    private Set<String> getUserRoles(UUID userId) {
        return new HashSet<>(securityUtils.getCurrentUserRoles());
    }
}
