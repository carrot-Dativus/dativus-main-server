package com.dativus.server.controller;

import com.dativus.server.entity.User;
import com.dativus.server.entity.Workspace;
import com.dativus.server.repository.UserRepository;
import com.dativus.server.service.WorkspaceService;
import com.dativus.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    // 💡 [핵심 추가] 토큰을 다시 찍어내기 위해 필요한 도구들을 가져옵니다.
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody Map<String, String> request) {
        Workspace ws = workspaceService.createWorkspace(request.get("name"));
        return ResponseEntity.ok(Map.of(
                "workspaceId", ws.getId().toString(),
                "inviteCode", ws.getInviteCode(),
                "message", "워크스페이스가 생성되었습니다."
        ));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinWorkspace(@RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(request.get("userId"));
        String inviteCode = request.get("inviteCode");

        // 1. 서비스에서 팀 합류 로직 실행 (DB 업데이트 됨)
        workspaceService.joinWorkspace(userId, inviteCode);

        // 2. 💡 [조용한 재발급 로직] 업데이트된 유저 정보로 '새 출입증'을 몰래 만들어냅니다.
        User user = userRepository.findById(userId).orElseThrow();
        String newWorkspaceId = user.getWorkspace().getId().toString();
        String newToken = jwtUtil.generateToken(user.getId().toString(), newWorkspaceId);

        // 3. 새 출입증과 새 방 번호를 리액트(프론트엔드)로 쏴줍니다!
        return ResponseEntity.ok(Map.of(
                "message", "팀 합류에 성공했습니다!",
                "access_token", newToken,
                "workspace_id", newWorkspaceId
        ));
    }
}