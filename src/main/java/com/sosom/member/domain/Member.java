package com.sosom.member.domain;

import com.sosom.baseentity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;
    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private SignUpType signUpType;

    private LocalDateTime lastActive;

    private Member(String email,String password,String nickname,MemberRole role,SignUpType signUpType){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.signUpType = signUpType;
        this.lastActive = LocalDateTime.now();
    }

    public static Member createNormalMember(String email,String password,String nickname){
        return new Member(email,password,nickname,MemberRole.ROLE_MEMBER,SignUpType.NORMAL);
    }

    public static Member createSocialMember(String email,String password,String nickname,SignUpType signUpType){
        return new Member(email,password,nickname,MemberRole.ROLE_MEMBER,signUpType);
    }

    public void changeNickname(String nickname){
        this.nickname = nickname;
    }
    public void updateLastActive(LocalDateTime now) {
        this.lastActive = now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return getEmail().equals(member.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }
}
