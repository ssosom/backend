package com.sosom.anotation;

import com.sosom.member.domain.MemberRole;
import com.sosom.security.userdetails.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomUserDetailsContextFactory implements WithSecurityContextFactory<WithCustomUserDetails> {
    @Override
    public SecurityContext createSecurityContext(WithCustomUserDetails annotation) {
        String email = annotation.email();
        String role = annotation.role();

        UserDetailsImpl userDetails = new UserDetailsImpl(email,"testPassword",MemberRole.valueOf(role));

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,userDetails.getPassword(),userDetails.getAuthorities());

        SecurityContext context  = SecurityContextHolder.getContext();

        context.setAuthentication(token);

        return context;

    }
}
