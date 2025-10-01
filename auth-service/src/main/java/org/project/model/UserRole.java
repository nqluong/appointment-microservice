package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_roles")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @NotNull(message = "Role cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    Role role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    LocalDateTime assignedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_by")
    User assignedBy;

    @Column(name = "is_active", nullable = false)
    boolean isActive = true;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;
}
