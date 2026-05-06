package com.dativus.server.service;

import com.dativus.server.dto.LoginRequest;
import com.dativus.server.dto.LoginResponse;
import com.dativus.server.entity.User;
import com.dativus.server.repository.UserRepository;
import com.dativus.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 확인 (현재는 평문 비교, 추후 암호화 적용)
        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        // 💡 [v4.0 핵심 방어 로직] 워크스페이스가 null일 경우를 안전하게 처리
        String workspaceId = null;
        if (user.getWorkspace() != null) {
            workspaceId = user.getWorkspace().getId().toString();
        }

        // 3. JWT 토큰 생성 (팀이 없으면 workspaceId 자리에 null이 들어감)
        String token = jwtUtil.generateToken(user.getId().toString(), workspaceId);

        // 4. 명세서에 맞게 응답 객체 만들어서 돌려주기
        return new LoginResponse(
                token,
                "Bearer",
                user.getId().toString(),
                workspaceId // 💡 여기도 에러 나던 것을 안전한 변수로 교체!
        );
    }
}