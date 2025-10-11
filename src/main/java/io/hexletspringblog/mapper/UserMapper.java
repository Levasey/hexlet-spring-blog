package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.UserCreateDTO;
import io.hexletspringblog.dto.UserDTO;
import io.hexletspringblog.dto.UserUpdateDTO;
import io.hexletspringblog.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    User toEntity(UserCreateDTO userCreateDTO);

    void updateEntityFromDTO(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
