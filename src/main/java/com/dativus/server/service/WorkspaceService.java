package com.dativus.server.service;

import com.dativus.server.entity.User;
import com.dativus.server.entity.Workspace;
import com.dativus.server.repository.UserRepository;
import com.dativus.server.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    // 1. 팀 생성 및 랜덤 초대코드 발급
    @Transactional
    public Workspace createWorkspace(String name) {
        // 무작위 UUID를 생성한 뒤 앞 6자리만 잘라서 대문자로 변환 (예: 8F3A1C)
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Workspace workspace = new Workspace(name, inviteCode);
        return workspaceRepository.save(workspace);
    }

    // 2. 초대코드를 이용한 팀 합류
    @Transactional
    public void joinWorkspace(UUID userId, String inviteCode) {
        // 1) 해당 초대코드를 가진 팀이 있는지 검색
        Workspace workspace = workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대코드입니다."));

        // 2) 합류하려는 유저가 존재하는지 검색
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 3) 유저에게 팀 소속 부여 (JPA의 '변경 감지' 덕분에 save() 호출 없이도 트랜잭션 종료 시 자동 반영됨)
        user.setWorkspace(workspace);
    }
}