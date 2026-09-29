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

    // email is normalised and password hashed by the service; the flags and
    // id belong to the database. Mapping the raw password here would put
    // plain text into the entity even briefly.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User toEntity(RegisterRequestDto request);
}
