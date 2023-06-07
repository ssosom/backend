package com.sosom.voice.domain;

import com.sosom.member.domain.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VOICE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    private String url;

    private String message;

    private Voice(Member member,String url,String message){
        this.member = member;
        this.url = url;
        this.message = message;
    }

    public static Voice createVoice(Member member,String url,String message){
        return new Voice(member,url,message);
    }
}
