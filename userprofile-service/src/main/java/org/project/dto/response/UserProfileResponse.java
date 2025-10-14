package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    UUID userProfileId;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Gender gender;
    String phone;
}
