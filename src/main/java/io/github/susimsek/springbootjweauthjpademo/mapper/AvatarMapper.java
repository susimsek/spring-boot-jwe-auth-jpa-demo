package io.github.susimsek.springbootjweauthjpademo.mapper;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AvatarDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.Avatar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvatarMapper {
    @Mapping(source = "updatedAt", target = "lastModified")
    AvatarDTO toAvatarDto(Avatar avatar);
}
