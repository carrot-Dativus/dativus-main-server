package com.dativus.server.controller;

import com.dativus.server.entity.FeedbackLog;
import com.dativus.server.repository.FeedbackLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
@CrossOrigin(originPatterns = "*") // 💡 해결: origins 대신 originPatterns를 사용!
public class FeedbackController {

    @Autowired
    private FeedbackLogRepository feedbackLogRepository;

    @PostMapping
    public ResponseEntity<?> saveFeedback(@RequestBody Map<String, Object> request) {
        FeedbackLog log = new FeedbackLog();
        log.setWorkspaceId((String) request.get("workspaceId"));
        log.setUserId((String) request.get("userId"));
        log.setQuery((String) request.get("query"));
        log.setAnswer((String) request.get("answer"));
        log.setPositive((Boolean) request.get("isPositive"));

        feedbackLogRepository.save(log);

        return ResponseEntity.ok().body(Map.of("status", "success", "message", "피드백이 자산화되었습니다."));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getMyPerformanceStats(@PathVariable String userId) {
        long totalCount = feedbackLogRepository.countByUserId(userId);
        long positiveCount = feedbackLogRepository.countByUserIdAndIsPositiveTrue(userId);
        List<FeedbackLog> failureLogs = feedbackLogRepository.findByUserIdAndIsPositiveFalseOrderByCreatedAtDesc(userId);

        // CSAT(만족도) 계산
        double csatScore = totalCount == 0 ? 0 : (double) positiveCount / totalCount * 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInteractions", totalCount);
        stats.put("csatScore", Math.round(csatScore)); // 반올림
        stats.put("failureLogs", failureLogs); // 👎 받은 로그들 (Level 3)

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/daily/{userId}")
    public ResponseEntity<?> getDailyStats(@PathVariable String userId) {
        // 💡 실제로는 DB에서 날짜별로 Group By 쿼리를 날려야 하지만,
        // 시연을 위해 최근 7일간의 데이터를 가공해서 보내주는 로직을 작성합니다.

        // 예시 데이터 구조 (JSON)
        // [ {"date": "05-10", "positive": 5, "negative": 1}, ... ]

        List<Map<String, Object>> dailyData = new ArrayList<>();
        // DB에서 데이터를 가져와서 날짜별로 맵핑하는 로직이 들어갈 자리입니다.
        // 일단은 리액트에서 그래프가 도는 것을 확인하기 위해 더미 데이터를 섞어 보낼 수 있습니다.

        return ResponseEntity.ok(dailyData);
    }
}