package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserProfileResponse {
    UUID id;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Gender gender;
    String address;
    String phone;
    String avatarUrl;
    LocalDateTime updatedAt;
}
