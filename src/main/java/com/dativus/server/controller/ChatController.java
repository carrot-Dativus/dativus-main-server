package com.dativus.server.controller;

import com.dativus.server.entity.ChatSession;
import com.dativus.server.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. 방 생성 또는 가져오기
    @PostMapping("/session")
    public ResponseEntity<?> getOrCreateSession(@RequestBody Map<String, String> request) {
        String workspaceId = request.get("workspaceId");
        String title = request.getOrDefault("title", "새 채팅방");
        ChatSession session = chatService.getOrCreateSession(workspaceId, title);

        return ResponseEntity.ok(Map.of("sessionId", session.getId().toString()));
    }

    // 2. 메시지 저장하기
    @PostMapping("/messages")
    public ResponseEntity<?> saveMessage(@RequestBody Map<String, String> request) {
        chatService.saveMessage(
                request.get("sessionId"),
                request.get("userId"),
                request.get("senderType"),
                request.get("senderName"),
                request.get("content")
        );
        return ResponseEntity.ok("메시지 저장 성공!");
    }

    // 3. 방 번호로 과거 대화 내역 불러오기
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<?> getChatHistory(@PathVariable String sessionId) {
        List<Map<String, String>> history = chatService.getChatHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}