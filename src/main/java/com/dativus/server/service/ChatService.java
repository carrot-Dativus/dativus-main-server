package com.dativus.server.service;

import com.dativus.server.entity.ChatMessage;
import com.dativus.server.entity.ChatSession;
import com.dativus.server.entity.User;
import com.dativus.server.entity.Workspace;
import com.dativus.server.repository.ChatMessageRepository;
import com.dativus.server.repository.ChatSessionRepository;
import com.dativus.server.repository.UserRepository;
import com.dativus.server.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    // 1. 방 만들기 (또는 기존 방 찾기)
    @Transactional
    public ChatSession getOrCreateSession(String workspaceIdStr, String title) {
        UUID workspaceId = UUID.fromString(workspaceIdStr);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));

        // 임시: 가장 최근 채팅방을 하나 가져오거나, 없으면 새로 만듭니다. (추후 방 목록 기능 추가 시 고도화)
        List<ChatSession> sessions = chatSessionRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
        if (!sessions.isEmpty()) {
            return sessions.get(0);
        }

        ChatSession newSession = new ChatSession();
        newSession.setWorkspace(workspace);
        newSession.setTitle(title);
        return chatSessionRepository.save(newSession);
    }

    // 2. 메시지 저장 (유저 또는 AI)
    @Transactional
    public void saveMessage(String sessionIdStr, String userIdStr, String senderType, String senderName, String content) {
        ChatSession session = chatSessionRepository.findById(UUID.fromString(sessionIdStr))
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        User user = null;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            user = userRepository.findById(UUID.fromString(userIdStr)).orElse(null);
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setUser(user);
        message.setSenderType(senderType); // "USER" 또는 "LOCAL_AI"
        message.setSenderName(senderName);
        message.setContent(content);

        chatMessageRepository.save(message);
    }

    // 3. 과거 대화 내역 불러오기
    public List<Map<String, String>> getChatHistory(String sessionIdStr) {
        UUID sessionId = UUID.fromString(sessionIdStr);
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 프론트엔드가 읽기 편한 형태로 포장해서 내려줍니다.
        return messages.stream().map(msg -> Map.of(
                "sender", msg.getSenderType().equals("USER") ? "user" : "ai",
                "text", msg.getContent()
        )).toList();
    }
}