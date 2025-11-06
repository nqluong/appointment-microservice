package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.project.dto.ApiResponse;
import org.project.dto.request.AssignRoleRequest;
import org.project.dto.request.UpdateRoleExpirationRequest;
import org.project.dto.response.RoleInfo;
import org.project.security.annotation.RequireOwnershipOrAdmin;
import org.project.security.jwt.principal.JwtUserPrincipal;
import org.project.service.RoleManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleManagementService roleManagementService;

    @GetMapping("/user/{userId}")
    @RequireOwnershipOrAdmin
    public ResponseEntity<ApiResponse<List<String>>> getUserRoles(@PathVariable UUID userId) {
        List<String> roles = roleManagementService.getUserRoles(userId);

        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách vai trò thành công")
                .data(roles)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication) {

        JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
        UUID assignedBy = principal.getUserId();

        roleManagementService.assignRoleToUser(request, assignedBy);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Gán vai trò cho người dùng thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/revoke/{userId}/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {

        roleManagementService.revokeRoleFromUser(userId, roleId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Thu hồi vai trò thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/revoke-all/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeAllUserRoles(@PathVariable UUID userId) {
        roleManagementService.revokeAllUserRoles(userId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Thu hồi tất cả vai trò thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleInfo>>> getAvailableRoles() {
        List<RoleInfo> roles = roleManagementService.getAvailableRoles();

        ApiResponse<List<RoleInfo>> apiResponse = ApiResponse.<List<RoleInfo>>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách vai trò có sẵn thành công")
                .data(roles)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update-expiration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateRoleExpiration(
            @Valid @RequestBody UpdateRoleExpirationRequest request) {

        roleManagementService.updateRoleExpiration(request);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Cập nhật thời hạn vai trò thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/check/{userId}/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkUserHasRole(
            @PathVariable UUID userId,
            @PathVariable String roleName) {

        boolean hasRole = roleManagementService.userHasRole(userId, roleName);

        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Kiểm tra vai trò thành công")
                .data(hasRole)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
