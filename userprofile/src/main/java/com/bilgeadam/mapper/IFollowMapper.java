package com.bilgeadam.mapper;

import com.bilgeadam.dto.request.CreateFollowRequestDto;
import com.bilgeadam.repository.entity.Follow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IFollowMapper {

    IFollowMapper INSTANCE = Mappers.getMapper(IFollowMapper.class);

    Follow toFollow(final String followid, final String userid);
}
