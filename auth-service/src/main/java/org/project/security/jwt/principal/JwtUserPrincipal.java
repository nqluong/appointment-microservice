package org.project.security.jwt.principal;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtUserPrincipal {
    UUID userId;
    String username;
    String email;
    List<String> roles;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role.toUpperCase());
    }

    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) return false;
        for (String role : roles) {
            if (this.roles.contains(role.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
