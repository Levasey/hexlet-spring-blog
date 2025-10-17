package io.hexletspringblog.mapper;

import io.hexletspringblog.dto.UserCreateDTO;
import io.hexletspringblog.dto.UserDTO;
import io.hexletspringblog.dto.UserUpdateDTO;
import io.hexletspringblog.model.User;
import org.mapstruct.*;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    UserDTO map(User user);

    User map(UserCreateDTO userCreateDTO);

    void update(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}
