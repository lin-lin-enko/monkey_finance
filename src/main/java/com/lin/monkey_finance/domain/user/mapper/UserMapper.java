package com.lin.monkey_finance.domain.user.mapper;

import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserUpdateDto;
import com.lin.monkey_finance.domain.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto userUpdateDto, @MappingTarget User user);

    UserResponseDto toResponseDto(User user);
}
