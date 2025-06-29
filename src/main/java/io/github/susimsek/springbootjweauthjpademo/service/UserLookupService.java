package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public User findByIdOrThrow(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with id: " + userId)
            );
    }

    @Transactional(readOnly = true)
    public User findByIdWithAuthoritiesOrThrow(String userId) {
        return userRepository.findOneWithAuthoritiesById(userId)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with id: " + userId)
            );
    }

    @Transactional(readOnly = true)
    public User findByUsernameWithAuthoritiesOrThrow(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found with username: " + username)
            );
    }
}
