package com.sosom.member.repository;

import com.sosom.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("Member 저장")
    void save(){
        //given
        Member member = Member.createNormalMember("email", "encode", "nickname");

        //when
        Member savedMember = memberRepository.save(member);

        //then
        assertThat(savedMember).isEqualTo(member);
    }

    @Nested
    @DisplayName("email로 Member찾기")
    class findOptionalByEmail{

        @Test
        @DisplayName("Member가 없는 경우")
        void not_exist_member(){
            //given
            Member member = Member.createNormalMember("email","password","nickname");
            memberRepository.save(member);

            //when
            Optional<Member> findMember = memberRepository.findOptionalByEmail("not_exist_email");

            //then
            assertThat(findMember.isPresent()).isFalse();
        }

        @Test
        @DisplayName("Member가 있는 경우")
        void exist_member(){
            //given
            Member member = Member.createNormalMember("email","password","nickname");
            memberRepository.save(member);

            //when
            Optional<Member> findMember = memberRepository.findOptionalByEmail("email");

            //then
            assertThat(findMember.isPresent()).isTrue();
            assertThat(findMember.get()).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("nickname으로 Member찾기")
    class findOptionalByNickname{

        @Test
        @DisplayName("Member가 없는 경우")
        void not_exist_member(){
            //given
            Member member = Member.createNormalMember("email","password","nickname");
            memberRepository.save(member);

            //when
            Optional<Member> findMember = memberRepository.findOptionalByNickname("not_exist_nickname");

            //then
            assertThat(findMember.isPresent()).isFalse();
        }

        @Test
        @DisplayName("Member가 있는 경우")
        void exist_member(){
            //given
            Member member = Member.createNormalMember("email","password","nickname");
            memberRepository.save(member);

            //when
            Optional<Member> findMember = memberRepository.findOptionalByNickname("nickname");

            //then
            assertThat(findMember.isPresent()).isTrue();
            assertThat(findMember.get()).isEqualTo(member);
        }
    }

}