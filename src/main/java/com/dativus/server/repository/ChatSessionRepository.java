package com.dativus.server.repository;

import com.dativus.server.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
}