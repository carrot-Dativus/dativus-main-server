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
    public ResponseEntity<?> saveMessage(@RequestBody Map<String, Object> request) {
        chatService.saveMessage(
                (String) request.get("sessionId"),
                (String) request.get("userId"),
                (String) request.get("senderType"),
                (String) request.get("senderName"),
                (String) request.get("content"),
                (Boolean) request.get("isPrivate"),
                // 💡 [신규 추가] 프론트가 보낸 모니터링 데이터 안전하게 변환해서 수신!
                request.get("latency") != null ? Double.valueOf(request.get("latency").toString()) : 0.0,
                request.get("tokens") != null ? Integer.valueOf(request.get("tokens").toString()) : 0
        );
        return ResponseEntity.ok("메시지 저장 성공!");
    }

    // 3. 방 번호로 과거 대화 내역 불러오기 (비밀/공용 분리)
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<?> getChatHistory(
            @PathVariable String sessionId,
            @RequestParam(required = false, defaultValue = "false") boolean isPrivate) { // 💡 [핵심] 탭에 따른 분리 요청 수신!
        List<Map<String, String>> history = chatService.getChatHistory(sessionId, isPrivate);
        return ResponseEntity.ok(history);
    }
}