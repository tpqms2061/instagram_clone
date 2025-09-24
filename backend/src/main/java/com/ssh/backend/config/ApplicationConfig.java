package com.ssh.backend.config;

import com.ssh.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;


    @Bean
    public UserDetailsService userDetailsService() {
        return loginId -> {
            try {
                // loginId가 숫자라면 userId 기반 조회
                Long userId = Long.parseLong(loginId);
                return userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with ID:  " + userId));
            } catch (NumberFormatException e) {
                // 숫자가 아니라면 email → username 순으로 조회
                return userRepository.findByEmail(loginId)
                        .or(() -> userRepository.findByUsername(loginId))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}