package org.project.mapper;

import org.mapstruct.*;
import org.project.dto.request.SpecialtyRequest;
import org.project.dto.request.SpecialtyUpdate;
import org.project.dto.response.SpecialtyResponse;
import org.project.model.Specialty;


import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SpecialtyMapper {
    Specialty toEntity(SpecialtyRequest dto);

    @Mapping(target = "specialtyId", source = "id")
    SpecialtyResponse toResponseDto(Specialty entity);

    List<SpecialtyResponse> toResponseDtoList(List<Specialty> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SpecialtyUpdate dto, @MappingTarget Specialty entity);
}
