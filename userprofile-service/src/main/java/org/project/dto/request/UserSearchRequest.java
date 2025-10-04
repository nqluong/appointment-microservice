package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchRequest {
    String userType;
    String keyword;
    Boolean isActive;
    Boolean isDeleted;
    String sortBy = "createdAt";
    String sortDirection = "DESC";
}
