package io.modicon.taskapp.infrastructure.security;

import io.modicon.taskapp.domain.repository.UserDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDataSource userDataSource;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDataSource.findById(username);
    }
}
