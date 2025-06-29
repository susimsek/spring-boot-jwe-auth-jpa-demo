package io.github.susimsek.springbootjweauthjpademo.mapper;

import io.github.susimsek.springbootjweauthjpademo.domain.Message;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.MessageDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    Message toEntity(CreateMessageRequestDTO dto);

    MessageDTO toMessageDto(Message entity);

    void updateMessage(UpdateMessageRequestDTO dto, @MappingTarget Message entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(PartialUpdateMessageRequestDTO dto, @MappingTarget Message entity);
}
