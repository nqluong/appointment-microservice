package org.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.dto.response.AvailableSlotInfo;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.project.repository.DoctorWithSlotsProjection;
import org.project.repository.SlotProjection;


@Mapper(componentModel = "spring")
public interface DoctorAvailabilityMapper {
    @Mapping(target = "availableSlots", ignore = true)
    @Mapping(target = "birthDate", source = "dateOfBirth")
    DoctorWithSlotsResponse toBaseDoctorResponse(DoctorWithSlotsProjection projection);

    @Mapping(target = "slotId", source = "slotId")
    @Mapping(target = "slotDate", source = "slotDate")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "isAvailable", source = "isAvailable")
    AvailableSlotInfo toAvailableSlotInfo(SlotProjection slotProjection);
}
