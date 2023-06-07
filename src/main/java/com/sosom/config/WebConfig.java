package com.sosom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosom.member.repository.MemberRepository;
import com.sosom.security.exception.CustomAuthenticationEntryPoint;
import com.sosom.security.jwt.JwtExceptionFilter;
import com.sosom.security.jwt.JwtTokenFilter;
import com.sosom.security.oauth2.Oauth2AuthenticationFailureHandler;
import com.sosom.security.oauth2.Oauth2AuthenticationSuccessHandler;
import com.sosom.security.oauth2.UserOAuth2ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebConfig {

    private final MemberRepository memberRepository;

    private final Oauth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    private final Oauth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

    private final ObjectMapper objectMapper;

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DefaultOAuth2UserService defaultOAuth2UserService(){
        return new UserOAuth2ServiceImpl(memberRepository,passwordEncoder());
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().antMatchers("/h2-console/**","/sosom/**","/v3/**","/ws/**"));
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new CustomAuthenticationEntryPoint(objectMapper);
    }


    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{

        http.csrf().disable();

        http.cors();

        http.httpBasic().disable();

        http
                .addFilterBefore(new JwtTokenFilter(secretKey),UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtExceptionFilter(objectMapper),JwtTokenFilter.class);

        http.authorizeRequests()
                .antMatchers(HttpMethod.POST,"/api/members","/api/login").permitAll()
                .antMatchers(HttpMethod.GET,"/api/members/nicknames","/api/members/emails").permitAll()
                .antMatchers(HttpMethod.PUT,"/api/refresh").permitAll()
                .anyRequest().authenticated();

        http.exceptionHandling()
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper));


        http.oauth2Login()
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureHandler(oauth2AuthenticationFailureHandler)
                .userInfoEndpoint()
                .userService(defaultOAuth2UserService());

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();

    }
}
