package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AvatarUploadDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.AvatarDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.Avatar;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.exception.ImageProcessingException;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceNotFoundException;
import io.github.susimsek.springbootjweauthjpademo.mapper.AvatarMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.AvatarRepository;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.security.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final UserRepository userRepository;
    private final AvatarRepository avatarRepository;
    private final UserLookupService userLookupService;
    private final AvatarMapper avatarMapper;
    private final UserCacheService userCacheService;

    public AvatarDTO getAvatar(String userId) {
        Avatar avatar = findImageOrThrow(userId);
        return avatarMapper.toAvatarDto(avatar);
    }

    @Transactional
    public AvatarUploadDTO uploadAvatar(String userId, MultipartFile file) {
        User user = userLookupService.findByIdOrThrow(userId);

        Avatar avatar = avatarRepository.findById(userId)
            .orElse(new Avatar());
        avatar.setUserId(userId);
        avatar.setContentType(file.getContentType());
        try {
            byte[] data = file.getBytes();
            avatar.setContent(data);
            String contentHash = HashUtils.sha256Hex(data);
            avatar.setContentHash(contentHash);
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image bytes", e);
        }

        avatar.setUser(user);
        user.setAvatar(avatar);
        Instant updatedAt = Instant.now();
        avatar.setUpdatedAt(updatedAt);
        String url = buildImageUrl(userId, updatedAt.toEpochMilli());
        user.setImageUrl(url);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);
        return new AvatarUploadDTO(url);
    }

    @Transactional
    public void deleteAvatar(String userId) {
        User user = userLookupService.findByIdOrThrow(userId);
        user.setAvatar(null);
        user.setImageUrl(null);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);
    }

    private Avatar findImageOrThrow(String userId) {
        return avatarRepository.findById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    ProblemType.IMAGE_NOT_FOUND,
                    "userId",
                    userId
                )
            );
    }

    private String buildImageUrl(String userId, long version) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/account/{userId}/avatar?v={version}")
            .buildAndExpand(userId, version)
            .toUriString();
    }
}
