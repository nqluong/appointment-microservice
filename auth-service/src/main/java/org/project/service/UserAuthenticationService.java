package org.project.service;


import org.project.model.User;

public interface UserAuthenticationService {
    User authenticateUser(String username, String password);
}
