package org.project.common.security.token;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GatewayUserPrincipal implements Principal, Serializable {
    UUID userId;
    String username;
    String email;
    List<String> roles;

    @Override
    public String getName() {
        return username;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role.toUpperCase());
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
