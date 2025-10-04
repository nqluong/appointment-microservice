package org.project.mapper;

import org.mapstruct.*;
import org.project.dto.request.CreateAbsenceRequest;
import org.project.dto.request.UpdateAbsenceRequest;
import org.project.dto.response.DoctorAbsenceResponse;
import org.project.model.DoctorAbsence;


import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorAbsenceMapper {

   // @Mapping(source = "doctor.id", target = "doctorUserId")
    DoctorAbsenceResponse toDto(DoctorAbsence entity);

    List<DoctorAbsenceResponse> toDtoList(List<DoctorAbsence> entities);

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//   // @Mapping(source = "doctorUserId", target = "doctor", qualifiedByName = "mapUserFromId")
//    DoctorAbsence toEntity(CreateAbsenceRequest request);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "doctor", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    void updateEntityFromRequest(UpdateAbsenceRequest request, @MappingTarget DoctorAbsence entity);

//    @Named("mapUserFromId")
//    default User mapUserFromId(UUID userId) {
//        if (userId == null) {
//            return null;
//        }
//        User user = new User();
//        user.setId(userId);
//        return user;
//    }
}

