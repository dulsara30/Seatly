package com.seatly.backend.user.mapper;

import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.user.model.User;
import com.seatly.backend.user.payload.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResponseDto toResponseDto(User user);

    // The service sets email and the hash; mapping the raw password would put plain text in the entity.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User toEntity(RegisterRequestDto request);
}
