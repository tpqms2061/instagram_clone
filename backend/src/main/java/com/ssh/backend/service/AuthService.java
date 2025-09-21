package com.ssh.backend.service;

import com.ssh.backend.dto.AuthRequest;
import com.ssh.backend.dto.AuthResponse;
import com.ssh.backend.dto.RegisterRequest;
import com.ssh.backend.dto.UserDto;
import com.ssh.backend.entity.AuthProvider;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.AuthenticationException;
import com.ssh.backend.exception.BadRequestException;
import com.ssh.backend.exception.UserAlreadyExistsException;
import com.ssh.backend.repository.UserRepository;
import com.ssh.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;         // DB 접근 (User CRUD)
    private final PasswordEncoder passwordEncoder;       // 비밀번호 암호화
    private final JwtService jwtService;                 // JWT 토큰 발급/검증
    private final AuthenticationManager authenticationManager; // 로그인 인증 관리 (여기서는 아직 사용 안함)

    // =========================
    // 회원가입 로직
    // =========================
    public AuthResponse register(RegisterRequest request) {
        // 1. username 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exist");
        }

        // 2. email 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exist");
        }

        // 3. User 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .provider(AuthProvider.LOCAL) // 로컬 가입자로 표기
                .build();

        // 4. DB에 저장
        user = userRepository.save(user);

        // 5. JWT Access Token & Refresh Token 생성
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 6. 응답 DTO(AuthResponse) 반환
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user)) // 엔티티 → DTO 변환
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            log.info(" auth service : " , request);
            // email, username 둘 중 하나로 로그인
            String loginId = request.getEmail() != null ? request.getEmail() : request.getUsername();

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginId,
                            request.getPassword()
                    )
            );
            User user = userRepository.findByEmail(loginId)
                    .or(() -> userRepository.findByUsername(loginId))
                    .orElseThrow(() -> new AuthenticationException("Authentication failed"));

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.fromEntity(user))
                    .build();

        } catch (BadRequestException e) {
            throw new AuthenticationException("Invalid email or password");
        }
    }
}