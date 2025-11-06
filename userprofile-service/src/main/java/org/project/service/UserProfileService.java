package org.project.service;

import org.project.events.UserRegisteredEvent;
import org.springframework.transaction.annotation.Transactional;

public interface UserProfileService {

    @Transactional
    void createEmptyProfile(UserRegisteredEvent event);
}
