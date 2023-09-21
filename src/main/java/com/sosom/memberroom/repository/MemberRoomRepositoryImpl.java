package com.sosom.memberroom.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sosom.member.domain.Member;
import com.sosom.memberroom.dto.GetRoomsDto;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.sosom.member.domain.QMember.member;
import static com.sosom.memberroom.domain.QMemberRoom.memberRoom;
import static com.sosom.room.domain.QRoom.room;
import static com.sosom.voice.domain.QVoice.voice;
import static com.sosom.voiceroom.domain.QVoiceRoom.voiceRoom;

@RequiredArgsConstructor
public class MemberRoomRepositoryImpl implements MemberRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetRoomsDto> findByMember(Member findMember) {

        StringPath lastActive = Expressions.stringPath("lastActive");

        return queryFactory.select(Projections.fields(GetRoomsDto.class,
                        room.id.as("roomId"),
                        memberRoom.roomName,
                        memberRoom.id.as("memberRoomId"),
                        ExpressionUtils.as(
                                lastActiveSubQuery(), "lastActive")))
                .from(memberRoom)
                .join(memberRoom.room, room)
                .join(memberRoom.member, member)
                .where(member.eq(findMember))
                .orderBy(lastActive.desc(),room.id.desc())
                .fetch();
    }

    private SubQueryExpression<LocalDateTime> lastActiveSubQuery() {
        return JPAExpressions
                .select(voice.createdDate)
                .from(voiceRoom)
                .join(voiceRoom.voice, voice)
                .where(voiceRoom.id.eq(
                                JPAExpressions
                                        .select(voiceRoom.id.max())
                                        .from(voiceRoom)
                                        .where(voiceRoom.room.eq(room))
                        )
                );
    }

}
