package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.Message;
import io.github.susimsek.springbootjweauthjpademo.dto.filter.MessageFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.MessageDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceAlreadyExistsException;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceNotFoundException;
import io.github.susimsek.springbootjweauthjpademo.mapper.MessageMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    public static final String MESSAGES_BY_LOCALE_CACHE = "messagesByLocale";

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageCacheService messageCacheService;

    @Cacheable(cacheNames = MESSAGES_BY_LOCALE_CACHE,
        key = "#locale.language", unless = "#result.isEmpty()")
    public Map<String, String> getMessages(Locale locale) {
        return loadMessagesFromDatabase(locale.getLanguage());
    }

    public Page<MessageDTO> listMessagesPaged(Pageable pageable, MessageFilter filter) {
        return messageRepository.findAll(filter.toSpecification(), pageable)
            .map(messageMapper::toMessageDto);
    }

    public MessageDTO getMessage(String id) {
        Message msg = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.MESSAGE_NOT_FOUND,
                "id",
                id
            ));
        return messageMapper.toMessageDto(msg);
    }

    @Transactional
    public MessageDTO createMessage(CreateMessageRequestDTO req) {
        if (messageRepository.existsByCodeAndLocale(req.code(), req.locale())) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MESSAGE_CODE_CONFLICT,
                "code",
                req.code()
            );
        }
        Message entity = messageMapper.toEntity(req);
        Message saved = messageRepository.save(entity);
        messageCacheService.clearMessageCache(saved.getLocale());
        return messageMapper.toMessageDto(saved);
    }

    @Transactional
    public MessageDTO updateMessage(String id, UpdateMessageRequestDTO req) {
        Message msg = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.MESSAGE_NOT_FOUND,
                "id",
                id
            ));
        if (!msg.getCode().equals(req.code())
            && messageRepository.existsByCodeAndLocale(req.code(), msg.getLocale())) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MESSAGE_CODE_CONFLICT,
                "code",
                req.code()
            );
        }
        messageMapper.updateMessage(req, msg);
        Message updated = messageRepository.save(msg);
        messageCacheService.clearMessageCache(updated.getLocale());
        return messageMapper.toMessageDto(updated);
    }

    @Transactional
    public MessageDTO patchMessage(String id, PartialUpdateMessageRequestDTO req) {
        Message msg = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.MESSAGE_NOT_FOUND,
                "id",
                id
            ));
        if (req.code() != null
            && !msg.getCode().equals(req.code())
            && messageRepository.existsByCodeAndLocale(req.code(), msg.getLocale())) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MESSAGE_CODE_CONFLICT,
                "code",
                req.code()
            );
        }
        messageMapper.partialUpdate(req, msg);
        Message updated = messageRepository.save(msg);
        messageCacheService.clearMessageCache(updated.getLocale());
        return messageMapper.toMessageDto(updated);
    }

    @Transactional
    public void deleteMessage(String id) {
        Message msg = messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.MESSAGE_NOT_FOUND,
                "id",
                id
            ));
        messageCacheService.clearMessageCache(msg.getLocale());
        messageRepository.delete(msg);
    }

    private Map<String, String> loadMessagesFromDatabase(String locale) {
        List<Message> messages = messageRepository.findByLocale(locale);
        return messages.stream()
            .collect(Collectors.toMap(Message::getCode, Message::getContent));
    }
}
