package com.dativus.server.service;

import com.dativus.server.dto.UserRegisterRequest;
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
public class UserService {

    private final UserRepository userRepository;
    // 💡 [핵심 1] 워크스페이스를 만들 권한을 가져옵니다.
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public String register(UserRegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 💡 [핵심 2] 가입 즉시 유저 전용 '개인 샌드박스'를 무조건 1개 자동 생성합니다!
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Workspace personalWorkspace = new Workspace(request.getUsername() + "의 샌드박스", inviteCode);
        workspaceRepository.save(personalWorkspace);

        // 💡 [핵심 3] 유저 생성 시 null 대신 방금 만든 개인 샌드박스를 강제로 소속시킵니다!
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                personalWorkspace
        );

        if (request.getPersona() != null) {
            user.updatePersona(
                    request.getPersona().getDecisionStyle(),
                    request.getPersona().getExpertise(),
                    request.getPersona().getTone()
            );
        }

        userRepository.save(user);
        return "회원가입 성공! 개인 샌드박스가 발급되었습니다. (User ID: " + user.getId() + ")";
    }

    @Transactional
    public void updateUserPersona(String userIdStr, String decisionStyle, String expertise, String tone) {
        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // User 엔티티에 이미 만들어져 있던 메서드 호출!
        user.updatePersona(decisionStyle, expertise, tone);
    }

    // 💡 [신규 추가] 유저 프로필 및 페르소나 조회 로직
    @Transactional(readOnly = true)
    public java.util.Map<String, String> getUserProfile(String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // DB에 값이 없으면(최초 가입 시) 기본값을 내려보내 줍니다.
        return java.util.Map.of(
                "email", user.getEmail(),
                "username", user.getUsername(),
                "decisionStyle", user.getPersonaDecisionStyle() != null ? user.getPersonaDecisionStyle() : "일반적인",
                "expertise", user.getPersonaExpertise() != null ? user.getPersonaExpertise() : "기본",
                "tone", user.getPersonaTone() != null ? user.getPersonaTone() : "친절한"
        );
    }
}