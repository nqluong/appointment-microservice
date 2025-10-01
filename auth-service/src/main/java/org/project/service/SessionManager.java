package org.project.service;

import org.project.dto.request.LogoutRequest;

public interface SessionManager {
    void terminateSession(LogoutRequest request);
}
