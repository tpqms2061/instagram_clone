package com.ssh.backend.controller;

import com.ssh.backend.dto.AuthResponse;
import com.ssh.backend.dto.RegisterRequest;
import com.ssh.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "${frontend.url}") //컨트롤별로 cros 설정을 주고 싶을떄 사용
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
            ) {
        return ResponseEntity.ok(authService.register(request));
    }
}
