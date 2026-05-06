package com.dativus.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList; // 💡 추가
import java.util.List;      // 💡 추가
import java.util.UUID;

@Entity
@Table(name = "workspaces")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // 💡 v4.0 추가: 팀원들이 입력하고 들어올 6자리 초대 코드
    @Column(name = "invite_code", unique = true, nullable = false)
    private String inviteCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 💡 v4.0 추가: 이 워크스페이스에 소속된 유저들 (1:N 관계)
    // CascadeType.ALL은 워크스페이스가 삭제될 때 연관된 설정도 관리하기 위함입니다.
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<User> members = new ArrayList<>();

    // 💡 서비스 코드에서 사용할 생성자 업데이트
    public Workspace(String name, String inviteCode) {
        this.name = name;
        this.inviteCode = inviteCode;
    }
}