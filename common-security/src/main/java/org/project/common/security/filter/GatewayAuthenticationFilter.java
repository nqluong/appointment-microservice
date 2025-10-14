package org.project.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.common.security.token.GatewayAuthenticationToken;
import org.project.common.security.token.GatewayUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_EMAIL = "X-Email";
    private static final String HEADER_ROLES = "X-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(HEADER_USER_ID);

            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                // Đọc thông tin từ headers
                UUID userId = UUID.fromString(userIdHeader);
                String username = request.getHeader(HEADER_USERNAME);
                String email = request.getHeader(HEADER_EMAIL);
                String rolesHeader = request.getHeader(HEADER_ROLES);

                List<String> roles = rolesHeader != null && !rolesHeader.isEmpty()
                        ? Arrays.asList(rolesHeader.split(","))
                        : List.of();

                // Tạo Principal
                GatewayUserPrincipal principal = GatewayUserPrincipal.builder()
                        .userId(userId)
                        .username(username)
                        .email(email)
                        .roles(roles)
                        .build();

                // Tạo Authorities
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                GatewayAuthenticationToken authentication =
                        new GatewayAuthenticationToken(principal, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security context established for user: {} with roles: {}",
                        username, roles);
            } else {
                log.debug("No user authentication headers found in request");
            }

        } catch (Exception e) {
            log.error("Error setting up security context from gateway headers", e);
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

}
