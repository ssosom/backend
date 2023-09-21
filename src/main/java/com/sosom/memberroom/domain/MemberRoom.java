package com.sosom.memberroom.domain;

import com.sosom.baseentity.BaseTimeEntity;
import com.sosom.member.domain.Member;
import com.sosom.room.domain.Room;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "update MEMBER_ROOM set DELETED = true where MEMBER_ROOM_ID = ?")
@Where(clause = "deleted = false")
public class MemberRoom extends BaseTimeEntity {

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

    private String roomName;

    private boolean deleted;

    private MemberRoom(Member member,Room room){
        this.member = member;
        this.room = room;
        this.roomName = "대화방 이름을 설정해주세요";
        this.deleted = false;
    }

    public static MemberRoom createMemberRoom(Member member,Room room){
        return new MemberRoom(member,room);
    }

    public void updateMemberRoomName(String roomName){
        this.roomName = roomName;
    }
}
