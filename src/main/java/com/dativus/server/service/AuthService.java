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

        // 2. 비밀번호 확인
        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        // 💡 [v4.0 개편 로직] 이제 유저는 여러 방에 속할 수 있으므로, 입장권(WorkspaceMember) 목록에서 정보를 꺼냅니다.
        String workspaceId = null;

        // 유저가 가진 입장권 목록이 비어있지 않은지 확인
        if (user.getWorkspaceMembers() != null && !user.getWorkspaceMembers().isEmpty()) {

            // [기존 코드 삭제] workspaceId = user.getWorkspaceMembers().get(0).getWorkspace().getId().toString();

            // 🎯 [신규 코드] 가입일(joinedAt) 기준으로 정렬해서 무조건 최초의 방(샌드박스)으로 입장시킵니다!
            workspaceId = user.getWorkspaceMembers().stream()
                    .min(java.util.Comparator.comparing(com.dativus.server.entity.WorkspaceMember::getJoinedAt))
                    .map(member -> member.getWorkspace().getId().toString())
                    .orElse(null);
        }

        // 3. JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getId().toString(), workspaceId);

        // 4. 응답 반환
        return new LoginResponse(
                token,
                "Bearer",
                user.getId().toString(),
                workspaceId
        );
    }
}