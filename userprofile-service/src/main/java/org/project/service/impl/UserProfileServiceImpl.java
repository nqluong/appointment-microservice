package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.events.UserRegisteredEvent;
import org.project.model.UserProfile;
import org.project.repository.UserProfileRepository;
import org.project.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileServiceImpl implements UserProfileService {

    UserProfileRepository userProfileRepository;

    @Transactional
    @Override
    public void createEmptyProfile(UserRegisteredEvent event) {
        // Kiểm tra xem profile đã tồn tại chưa (idempotency)
        if (userProfileRepository.existsByUserId(event.getUserId())) {
            log.warn("Profile already exists for userId: {}", event.getUserId());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .build();

        userProfileRepository.save(profile);
        log.info("Created empty profile for userId: {} with name: {} {}",
                event.getUserId(), event.getFirstName(), event.getLastName());
    }
}
