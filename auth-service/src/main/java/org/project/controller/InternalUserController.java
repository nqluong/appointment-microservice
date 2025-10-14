package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.dto.response.UserIdsResponse;
import org.project.dto.response.UserValidationResponse;
import org.project.model.User;
import org.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/{userId}/validate")
    public ResponseEntity<UserValidationResponse> validateUser(
            @PathVariable UUID userId,
            @RequestParam String role) {

        log.info("Validating user {} with role {}", userId, role);

        UserValidationResponse response = userService.validateUserWithRole(userId, role);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/basic")
    public ResponseEntity<UserBasicInfoResponse> getUserBasicInfo(@PathVariable UUID userId) {
        log.debug("Getting basic info for user {}", userId);

        UserBasicInfoResponse response = userService.getUserBasicInfo(userId);

        return ResponseEntity.ok(response);
    }

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

    @GetMapping("/{userId}/has-role/{roleName}")
    public ResponseEntity<Boolean> hasRole(
            @PathVariable UUID userId,
            @PathVariable String roleName) {
        return ResponseEntity.ok(userService.hasRole(userId, roleName));
    }

    @GetMapping("/{userId}/has-all-roles")
    public ResponseEntity<Boolean> hasAllRoles(
            @PathVariable UUID userId,
            @RequestParam List<String> roles) {
        return ResponseEntity.ok(userService.hasAllRoles(userId, roles));
    }

    @GetMapping("/{userId}/has-any-role")
    public ResponseEntity<Boolean> hasAnyRole(
            @PathVariable UUID userId,
            @RequestParam List<String> roles) {
        return ResponseEntity.ok(userService.hasAnyRole(userId, roles));
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<Boolean> isActive(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.isActive(userId));
    }
}
