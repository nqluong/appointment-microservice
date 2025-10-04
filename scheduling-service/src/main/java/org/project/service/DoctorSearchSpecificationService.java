package org.project.service;

import org.project.dto.request.DoctorSearchRequest;
import org.springframework.data.jpa.domain.Specification;

public interface DoctorSearchSpecificationService {
    /**
     * Specification để tìm kiếm bác sĩ theo các tiêu chí
     * @param request yêu cầu tìm kiếm
     * @return Specification để thực hiện query
     */
   // Specification<User> buildDoctorSearchSpecification(DoctorSearchRequest request);

}
