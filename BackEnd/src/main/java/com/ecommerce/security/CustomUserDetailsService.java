package com.ecommerce.security;

import com.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrId) throws UsernameNotFoundException {
        return userRepository.findByEmail(emailOrId)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + emailOrId));
    }

    public UserDetails loadUserById(Long id) {
        return userRepository.findById(id)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User id không tồn tại: " + id));
    }
}
