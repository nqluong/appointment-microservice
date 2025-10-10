package org.project.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckRolesRequest {
    @NotEmpty(message = "Danh sách roles không được rỗng")
    List<String> roles;

    @Builder.Default
    boolean requireAll = false;
}
