package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AuthorityDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.filter.AuthorityFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceConflictException;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceNotFoundException;
import io.github.susimsek.springbootjweauthjpademo.mapper.AuthorityMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.AuthorityRepository;
import io.github.susimsek.springbootjweauthjpademo.repository.UserAuthorityMappingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorityService {
    private final AuthorityRepository authorityRepository;
    private final AuthorityMapper authorityMapper;
    private final UserAuthorityMappingRepository userAuthorityMappingRepository;
    private final AuthorityCacheService authorityCacheService;

    public Page<AuthorityDTO> listAuthoritiesPaged(Pageable pageable,
                                                   AuthorityFilter filter) {

        return authorityRepository.findAll(filter.toSpecification(), pageable)
            .map(authorityMapper::toAuthorityDto);
    }

    public List<AuthorityDTO> getAllAuthorities() {
        return authorityRepository.findAll()
            .stream()
            .map(authorityMapper::toAuthorityDto)
            .toList();
    }

    public AuthorityDTO getAuthority(String id) {
        Authority a = authorityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.AUTHORITY_NOT_FOUND,
                "id",
                id
            ));
        return authorityMapper.toAuthorityDto(a);
    }

    @Transactional
    public AuthorityDTO createAuthority(CreateAuthorityRequestDTO req) {
        if (authorityRepository.existsByName(req.name())) {
            throw new ResourceConflictException(
                ProblemType.AUTHORITY_NAME_CONFLICT,
                "name",
                req.name()
            );
        }
        Authority authority = authorityMapper.toEntity(req);
        Authority saved = authorityRepository.save(authority);
        authorityCacheService.clearAuthorityCaches(saved);
        return authorityMapper.toAuthorityDto(saved);
    }

    @Transactional
    public AuthorityDTO updateAuthority(String id, UpdateAuthorityRequestDTO req) {
        Authority a = authorityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.AUTHORITY_NOT_FOUND,
                "id",
                id
            ));
        if (!a.getName().equals(req.name()) && authorityRepository.existsByName(req.name())) {
            throw new ResourceConflictException(
                ProblemType.AUTHORITY_NAME_CONFLICT,
                "name",
                req.name()
            );
        }
        authorityMapper.updateAuthority(req, a);
        Authority updated = authorityRepository.save(a);
        authorityCacheService.clearAuthorityCaches(updated);
        return authorityMapper.toAuthorityDto(updated);
    }

    @Transactional
    public AuthorityDTO patchAuthority(String id, PartialUpdateAuthorityRequestDTO req) {
        Authority a = authorityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.AUTHORITY_NOT_FOUND,
                "id",
                id
            ));
        if (req.name() != null && !a.getName().equals(req.name())
            && authorityRepository.existsByName(req.name())) {
            throw new ResourceConflictException(
                ProblemType.AUTHORITY_NAME_CONFLICT,
                "name",
                req.name()
            );
        }
        authorityMapper.partialUpdate(req, a);
        Authority updated = authorityRepository.save(a);
        authorityCacheService.clearAuthorityCaches(updated);
        return authorityMapper.toAuthorityDto(updated);
    }

    @Transactional
    public void deleteAuthority(String id) {
        Authority a = authorityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.AUTHORITY_NOT_FOUND,
                "id",
                id
            ));
        if (a.isProtectedFlag()) {
            throw new ResourceConflictException(
                ProblemType.AUTHORITY_PROTECTED,
                "protected",
                id
            );
        }
        long count = userAuthorityMappingRepository.countByAuthorityId(id);
        if (count > 0) {
            throw new ResourceConflictException(
                ProblemType.AUTHORITY_ASSIGNED_CONFLICT,
                "protected"
            );
        }
        authorityCacheService.clearAuthorityCaches(a);
        authorityRepository.delete(a);
    }
}
