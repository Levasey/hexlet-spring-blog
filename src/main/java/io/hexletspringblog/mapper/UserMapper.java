package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.UserCreateDTO;
import io.hexletspringblog.dto.UserDTO;
import io.hexletspringblog.dto.UserUpdateDTO;
import io.hexletspringblog.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "role", expression = "java(io.hexletspringblog.model.UserRole.USER)")
    User toEntity(UserCreateDTO userCreateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "birthday", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordDigest", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateEntityFromDTO(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
