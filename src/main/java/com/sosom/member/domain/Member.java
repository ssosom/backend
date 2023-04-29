package com.sosom.member.domain;

import com.sosom.baseentity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    private Member(String email,String password,String nickname,MemberRole role,SignUpType signUpType){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.signUpType = signUpType;
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



}
