package org.project.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.project.dto.request.SpecialtyRequest;
import org.project.dto.request.SpecialtyUpdate;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.SpecialtyResponse;
import org.project.model.MedicalProfile;
import org.project.model.Specialty;
import org.project.model.UserProfile;
import org.project.repository.MedicalProfileRepository;
import org.project.repository.UserProfileRepository;
import org.project.service.AvatarUrlService;
import org.project.util.NameUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class SpecialtyMapper {

    @Autowired
    protected MedicalProfileRepository medicalProfileRepository;
    
    @Autowired
    protected UserProfileRepository userProfileRepository;
    
    @Autowired
    protected AvatarUrlService avatarUrlService;

    public abstract Specialty toEntity(SpecialtyRequest dto);

    @Mapping(target = "specialtyId", source = "id")
    @Mapping(target = "doctors", expression = "java(mapDoctors(entity.getId()))")
    @Mapping(target = "doctorCount", ignore = true)
    public abstract SpecialtyResponse toResponseDto(Specialty entity);

    @Mapping(target = "specialtyId", source = "id")
    @Mapping(target = "doctors", ignore = true)
    @Mapping(target = "doctorCount", expression = "java(getDoctorCount(entity.getId()))")
    public abstract SpecialtyResponse toResponseDtoWithCount(Specialty entity);

    public List<SpecialtyResponse> toResponseDtoListWithCount(List<Specialty> entities) {
        return entities.stream()
            .map(this::toResponseDtoWithCount)
            .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntityFromDto(SpecialtyUpdate dto, @MappingTarget Specialty entity);
    
    protected Integer getDoctorCount(UUID specialtyId) {
        Long count = medicalProfileRepository.countApprovedDoctorsBySpecialtyId(specialtyId);
        return count != null ? count.intValue() : 0;
    }
    
    protected List<DoctorResponse> mapDoctors(UUID specialtyId) {
        List<MedicalProfile> medicalProfiles = medicalProfileRepository.findApprovedMedicalProfilesBySpecialtyId(specialtyId);
        
        if (medicalProfiles.isEmpty()) {
            return List.of();
        }
        
        // Lấy tất cả user profiles cùng lúc
        List<UUID> userIds = medicalProfiles.stream()
            .map(MedicalProfile::getUserId)
            .collect(Collectors.toList());
        
        Map<UUID, UserProfile> userProfileMap = userProfileRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(UserProfile::getUserId, up -> up));
        
        // Lấy tất cả avatar file names
        List<String> avatarFileNames = userProfileMap.values().stream()
            .map(UserProfile::getAvatarUrl)
            .filter(avatarUrl -> avatarUrl != null && !avatarUrl.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        
        // Tạo batch presigned URLs
        Map<String, String> avatarUrlMap = avatarUrlService.generateBatchPresignedUrls(avatarFileNames);
        
        // Map sang DoctorResponse
        return medicalProfiles.stream()
            .map(mp -> {
                UserProfile userProfile = userProfileMap.get(mp.getUserId());
                
                if (userProfile == null) {
                    return null;
                }
                
                String fullName = NameUtils.formatDoctorFullName(
                    userProfile.getFirstName() + " " + userProfile.getLastName()
                );
                
                String avatarUrl = userProfile.getAvatarUrl() != null 
                    ? avatarUrlMap.get(userProfile.getAvatarUrl())
                    : null;
                
                return DoctorResponse.builder()
                    .userId(userProfile.getUserId())
                    .fullName(fullName)
                    .gender(userProfile.getGender() != null ? userProfile.getGender().toString() : null)
                    .phone(userProfile.getPhone())
                    .avatarUrl(avatarUrl)
                    .qualification(mp.getQualification())
                    .yearsOfExperience(mp.getYearsOfExperience())
                    .consultationFee(mp.getConsultationFee())
                    .specialtyName(mp.getSpecialty().getName())
                    .approved(mp.isDoctorApproved())
                    .build();
            })
            .filter(doctor -> doctor != null)
            .collect(Collectors.toList());
    }
}
