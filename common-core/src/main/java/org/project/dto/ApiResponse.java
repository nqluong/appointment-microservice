package org.project.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse <T>{
    private boolean success;
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
