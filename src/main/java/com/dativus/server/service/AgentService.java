package com.dativus.server.service;

import com.dativus.server.entity.Agent;
import com.dativus.server.entity.User;
import com.dativus.server.repository.AgentRepository;
import com.dativus.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    // 💡 1. 새로운 자아를 빚어내는 로직
    @Transactional
    public Agent createAgent(String ownerIdStr, String name, String description, String agentType) {
        // 주인이 진짜 존재하는지 확인
        User owner = userRepository.findById(UUID.fromString(ownerIdStr))
                .orElseThrow(() -> new RuntimeException("지휘관(유저)을 찾을 수 없습니다."));

        // 새로운 에이전트(자아) 생성 및 속성 부여
        Agent agent = new Agent();
        agent.setOwner(owner);
        agent.setName(name);
        agent.setDescription(description); // 성격, 역할, 프롬프트 등
        agent.setAgentType(agentType);     // 예: "LOCAL" (파이썬 Llama3 기반)
        agent.setModelName("llama3");      // 기본 장착 뇌 모델
        agent.setIsActive(true);

        return agentRepository.save(agent);
    }

    // 💡 2. 내가 만든 자아 목록 불러오기 로직
    @Transactional(readOnly = true)
    public List<Agent> getAgentsByOwner(String ownerIdStr) {
        return agentRepository.findByOwnerId(UUID.fromString(ownerIdStr));
    }
}