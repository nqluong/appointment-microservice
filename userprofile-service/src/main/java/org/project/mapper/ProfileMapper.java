package org.project.mapper;

import org.mapstruct.*;
import org.project.dto.response.CompleteProfileProjection;
import org.project.dto.response.CompleteProfileResponse;
import org.project.dto.response.UserProfileResponse;
import org.project.model.UserProfile;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProfileMapper {
    CompleteProfileResponse toCompleteProfileResponse(CompleteProfileProjection user);

    @Mapping(target = "userProfileId", source = "id")
    UserProfileResponse toUserProfileResponse(UserProfile user);
}
