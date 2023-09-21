package com.sosom.security.userdetails;

import com.sosom.member.domain.Member;
import com.sosom.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findOptionalByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("존재하지 않는 유저입니다"));
        return new UserDetailsImpl(member.getEmail(), member.getPassword(), member.getRole());
    }
}
