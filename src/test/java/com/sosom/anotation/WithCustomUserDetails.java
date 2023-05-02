package com.sosom.anotation;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomUserDetailsContextFactory.class)
public @interface WithCustomUserDetails {
    String email();
    String role();
}
