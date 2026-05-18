package com.dativus.server.service;

import com.dativus.server.entity.*;
import com.dativus.server.repository.*;
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
    private final FeedbackLogRepository feedbackLogRepository;

    // 1. 방 만들기 (또는 기존 방 찾기)
    @Transactional
    public ChatSession getOrCreateSession(String workspaceIdStr, String title) {
        UUID workspaceId = UUID.fromString(workspaceIdStr);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다."));

        // 1. 해당 워크스페이스에 이미 생성된 세션이 있는지 확인합니다.
        List<ChatSession> existingSessions = chatSessionRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);

        // 2. 이미 사용하던 방이 있다면, 가장 최근 방을 반환하여 대화를 이어가게 합니다.
        if (existingSessions != null && !existingSessions.isEmpty()) {
            return existingSessions.get(0);
        }

        ChatSession newSession = new ChatSession();
        newSession.setWorkspace(workspace);
        newSession.setTitle(title);
        return chatSessionRepository.save(newSession);
    }

    // 2. 메시지 저장 (비밀 여부 포함)
    @Transactional
    public void saveMessage(String sessionIdStr, String userIdStr, String senderType, String senderName, String content, Boolean isPrivate, Double latency, Integer tokens) {
        ChatSession session = chatSessionRepository.findById(UUID.fromString(sessionIdStr))
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        User user = null;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            user = userRepository.findById(UUID.fromString(userIdStr)).orElse(null);
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setUser(user);
        message.setSenderType(senderType);
        message.setSenderName(senderName);
        message.setContent(content);
        message.setPrivate(isPrivate != null ? isPrivate : false);
        message.setLatency(latency != null ? latency : 0.0);
        message.setTokens(tokens != null ? tokens : 0);

        chatMessageRepository.save(message);

        // =========================================================
        // 작전 5: AI 자동 실패 감지 룰 엔진 (Rule-Engine)
        // =========================================================
        if ("LOCAL_AI".equals(senderType)) {
            boolean isAutoFail = false;
            String failReason = "";

            // 룰 1: 작전 수행 시간이 30초를 초과한 경우
            if (latency != null && latency > 30.0) {
                isAutoFail = true;
                failReason = "응답 지연 초과 (" + latency + "초)";
            }
            // 룰 2: 환각(Hallucination) 및 회피 키워드 감지
            else if (content.contains("잘 모르겠습니다") ||
                    content.contains("알 수 없습니다") ||
                    content.contains("정보가 없습니다")) {
                isAutoFail = true;
                failReason = "환각/회피성 키워드 감지";
            }

            // 감지기에 걸렸다면 강제로 오답 노트(FeedbackLog) 적재!
            if (isAutoFail) {
                FeedbackLog autoFeedback = new FeedbackLog();

                autoFeedback.setUserId(userIdStr);

                // 💡 1. Session 객체에서 Workspace ID를 꺼내와서 세팅 (Entity 구조 일치)
                if (session.getWorkspace() != null) {
                    autoFeedback.setWorkspaceId(session.getWorkspace().getId().toString());
                }

                // 💡 2. 필수 값(query, answer) 세팅
                // - query(질문) 칸에는 관리자가 보기 편하게 [자동 감지 사유]를 기록합니다.
                autoFeedback.setQuery("[시스템 자동 감지 사유] " + failReason);
                // - answer(답변) 칸에는 30초가 넘었거나 헛소리를 한 문제의 AI 답변 전체를 박아넣습니다.
                autoFeedback.setAnswer(content);

                // 💡 3. Lombok 규칙에 맞게 세터 이름 변경
                autoFeedback.setPositive(false);

                feedbackLogRepository.save(autoFeedback);

                System.out.println("🚨 [시스템 자동 감지] 기준 미달 답변 적발! 오답 노트 강제 적재 완료 ➔ 사유: " + failReason);
            }
        }
    }

    // 3. 과거 대화 내역 불러오기 (비밀/공용 필터링)
    public List<Map<String, String>> getChatHistory(String sessionIdStr, boolean isPrivate) {
        UUID sessionId = UUID.fromString(sessionIdStr);

        // 💡 [핵심] 기존 findBySessionIdOrderByCreatedAtAsc 대신 비밀 여부까지 확인하는 전용 탐지기 사용!
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdAndIsPrivateOrderByCreatedAtAsc(sessionId, isPrivate);

        return messages.stream().map(msg -> Map.of(
                "sender", msg.getSenderType().equals("USER") ? "user" : "ai",
                "text", msg.getContent()
        )).toList();
    }
}