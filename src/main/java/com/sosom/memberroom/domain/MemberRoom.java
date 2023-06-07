package com.sosom.memberroom.domain;

import com.sosom.member.domain.Member;
import com.sosom.room.domain.Room;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ROOM_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOM_ID")
    private Room room;

    private MemberRoom(Member member,Room room){
        this.member = member;
        this.room = room;
    }

    public static MemberRoom createMemberRoom(Member member,Room room){
        return new MemberRoom(member,room);
    }
}
