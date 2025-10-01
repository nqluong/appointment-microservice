package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.project.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "First name can only contain letters and spaces")
    @Column(name = "first_name", nullable = false)
    String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Last name can only contain letters and spaces")
    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Column(name = "address")
    String address;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone")
    String phone;

    @Column(name = "avatar_url")
    String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;


    @Column(name = "user_id", nullable = false)
    UUID userId;
}
