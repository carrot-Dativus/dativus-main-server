package com.dativus.server.controller;

import com.dativus.server.dto.LoginRequest;
import com.dativus.server.dto.LoginResponse;
import com.dativus.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 에러가 나면 401(인증 실패) 상태 코드와 함께 메시지를 보냅니다. [cite: 160]
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}