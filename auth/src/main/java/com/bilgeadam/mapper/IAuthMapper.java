package com.bilgeadam.mapper;

import com.bilgeadam.dto.request.NewCreateUserRequestDto;
import com.bilgeadam.dto.request.RegisterRequestDto;
import com.bilgeadam.dto.request.UpdateEmailOrUsernameRequestDto;
import com.bilgeadam.dto.response.RegisterResponseDto;
import com.bilgeadam.rabbitmq.model.RegisterMailModel;
import com.bilgeadam.rabbitmq.model.RegisterModel;
import com.bilgeadam.repository.entity.Auth;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IAuthMapper {
    IAuthMapper INSTANCE = Mappers.getMapper(IAuthMapper.class);

    Auth fromRequestDtoToAuth(RegisterRequestDto dto);
    RegisterResponseDto fromAuthToResponseDto(Auth auth);

    @Mapping(source = "id", target = "authid")
    NewCreateUserRequestDto fromAuthtoNewCreateUser(final Auth auth);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Auth fromUpdateDtotoAuth(UpdateEmailOrUsernameRequestDto dto, @MappingTarget Auth auth);

    @Mapping(source = "id", target = "authid")
    RegisterModel fromAuthToRegisterModel(final Auth auth);

    RegisterMailModel fromAuthToRegisterMailModel(final Auth auth);
}
