package org.project.mapper;

import org.mapstruct.*;
import org.project.dto.response.DoctorResponse;
import org.project.repository.DoctorProjection;
import org.project.util.NameUtils;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorMapper {

    @Mapping(target = "fullName", qualifiedByName = "formatDoctorTitle")
    DoctorResponse projectionToResponse(DoctorProjection projection);

    @Named("formatDoctorTitle")
    default String formatDoctorTitle(String fullName) {
        return NameUtils.formatDoctorFullName(fullName);
    }
}
