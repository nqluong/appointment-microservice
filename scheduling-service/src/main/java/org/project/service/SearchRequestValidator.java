package org.project.service;


import org.project.dto.request.DoctorSearchRequest;

public interface SearchRequestValidator {
    // Validate các tham số tìm kiếm
    void validateSearchRequest(DoctorSearchRequest request);
}
