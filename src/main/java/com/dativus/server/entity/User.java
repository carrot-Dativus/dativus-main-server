package com.dativus.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 👈 1. 롬복 추가

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor // 👈 2. 어노테이션 추가
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // v4.0 페르소나 컬럼 추가 (null 허용)
    @Column(name = "persona_decision_style")
    private String personaDecisionStyle;

    @Column(name = "persona_expertise")
    private String personaExpertise;

    @Column(name = "persona_tone")
    private String personaTone;

    public void updatePersona(String decisionStyle, String expertise, String tone) {
        this.personaDecisionStyle = decisionStyle;
        this.personaExpertise = expertise;
        this.personaTone = tone;
    }

    // 👈 3. 서비스 코드 에러를 없애줄 진짜 핵심 생성자! (ID는 제외)
    public User(String username, String email, String passwordHash, Workspace workspace) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.workspace = workspace;

    }

    // 💡 팀 합류를 위한 편의 메서드
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}