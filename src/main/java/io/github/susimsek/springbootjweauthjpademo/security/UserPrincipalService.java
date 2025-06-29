package io.github.susimsek.springbootjweauthjpademo.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserPrincipalService extends UserDetailsService {

    UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException;
}
