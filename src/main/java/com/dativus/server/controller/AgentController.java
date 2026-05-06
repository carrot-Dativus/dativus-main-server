package com.dativus.server.controller;

import com.dativus.server.entity.Agent;
import com.dativus.server.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 💡 CORS 방어막 해제 (프리패스 모드)
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    // 🟢 1. 자아 생성 창구 (POST)
    @PostMapping
    public ResponseEntity<?> createAgent(@RequestBody Map<String, String> request) {
        try {
            Agent agent = agentService.createAgent(
                    request.get("userId"),
                    request.get("name"),
                    request.get("description"),
                    request.get("agentType")
            );
            return ResponseEntity.ok(Map.of(
                    "message", "새로운 자아가 성공적으로 탄생했습니다!",
                    "agentId", agent.getId().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🟢 2. 내 자아 목록 조회 창구 (GET)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAgents(@PathVariable String userId) {
        try {
            List<Agent> agents = agentService.getAgentsByOwner(userId);

            // 프론트엔드가 쓰기 좋게 포장해서 넘겨줍니다.
            List<Map<String, Object>> result = agents.stream().map(a -> Map.<String, Object>of(
                    "id", a.getId().toString(),
                    "name", a.getName(),
                    "description", a.getDescription(),
                    "agentType", a.getAgentType()
            )).toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("자아 목록을 불러오는 데 실패했습니다.");
        }
    }
}