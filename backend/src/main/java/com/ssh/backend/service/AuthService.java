package com.ssh.backend.service;

import com.ssh.backend.dto.AuthRequest;
import com.ssh.backend.dto.AuthResponse;
import com.ssh.backend.dto.RegisterRequest;
import com.ssh.backend.dto.UserDto;
import com.ssh.backend.entity.AuthProvider;
import com.ssh.backend.exception.UserAlreadyExistsException;
import com.ssh.backend.repository.UserRepository;
import com.ssh.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ssh.backend.entity.User;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    // register  필요 ->  AuthRequest , AuthResponse 필요함

    public AuthResponse register(RegisterRequest request) {

        //중복체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");

        }

        //생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .provider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);

        //토큰 바급
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user))
                .build();
    }
}
