package org.project.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegisteredEvent {
    UUID userId;
    String email;
    String firstName;
    String lastName;
    String username;
}
