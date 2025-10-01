package org.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.TokenType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "invalidated_tokens")
public class InvalidatedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "token_hash", nullable = false)
     String tokenHash;

    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "black_listed_at")
     LocalDateTime blackListedAt;

    @Column(name = "ip_address")
     String ipAddress;

    @Column(name = "user_agent")
     String userAgent;

    @Column(name = "reason")
     String reason;
}
