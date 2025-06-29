package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.exception.AccountLockedException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DomainUserDetailsService implements UserPrincipalService {

    private final UserLookupService userLookupService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userLookupService.findByUsernameWithAuthoritiesOrThrow(username);
        UserDetails userDetails = userMapper.toPrincipal(user);
        if (!userDetails.isAccountNonLocked()) {
            throw new AccountLockedException(ProblemType.ACCOUNT_LOCKED, user.getLockExpiresAt());
        }
        return userDetails;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        User user = userLookupService.findByIdWithAuthoritiesOrThrow(userId);
        return userMapper.toPrincipal(user);
    }
}
