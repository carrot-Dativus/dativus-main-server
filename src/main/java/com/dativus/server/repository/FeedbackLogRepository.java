package com.dativus.server.repository;

import com.dativus.server.entity.FeedbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FeedbackLogRepository extends JpaRepository<FeedbackLog, UUID> {
    // 특정 유저의 피드백을 최신순으로 정렬
    List<FeedbackLog> findByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 유저의 부정적 피드백(오답 노트)만 추출
    List<FeedbackLog> findByUserIdAndIsPositiveFalseOrderByCreatedAtDesc(String userId);

    // 통계를 위한 카운트
    long countByUserId(String userId);
    long countByUserIdAndIsPositiveTrue(String userId);
}