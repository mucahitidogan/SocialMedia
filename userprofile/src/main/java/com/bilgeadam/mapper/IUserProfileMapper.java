package com.bilgeadam.mapper;

import com.bilgeadam.dto.request.NewCreateUserRequestDto;
import com.bilgeadam.dto.request.ToAuthPasswordChangeDto;
import com.bilgeadam.dto.request.UpdateEmailOrUsernameRequestDto;
import com.bilgeadam.dto.request.UserProfileUpdateRequestDto;
import com.bilgeadam.rabbitmq.model.RegisterElasticModel;
import com.bilgeadam.rabbitmq.model.RegisterModel;
import com.bilgeadam.repository.entity.UserProfile;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IUserProfileMapper {
    IUserProfileMapper INSTANCE = Mappers.getMapper(IUserProfileMapper.class);

    UserProfile fromDtotoUserProfile(final NewCreateUserRequestDto dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserProfile updateUserfromDto(UserProfileUpdateRequestDto dto, @MappingTarget UserProfile userProfile);

    UpdateEmailOrUsernameRequestDto fromUpdateDtotoEmailUsernameDto(final UserProfileUpdateRequestDto dto);

    UserProfile fromRegisterModelToUser(final RegisterModel model);

    RegisterElasticModel fromUserToElasticModel(final UserProfile userProfile);

    ToAuthPasswordChangeDto fromUserProfileToAuthChangePasswordDto(final UserProfile userProfile);
}
