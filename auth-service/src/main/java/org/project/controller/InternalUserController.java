package org.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.dto.response.UserIdsResponse;
import org.project.model.User;
import org.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/by-role/{roleName}")
    public ResponseEntity<UserIdsResponse> getUserIdsByRole(@PathVariable String roleName) {
        UserIdsResponse response = userService.getUserIdsByRole(roleName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getById(@PathVariable UUID userId) {
        User response = userService.findByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> existsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.existsByUserId(userId));
    }
}
