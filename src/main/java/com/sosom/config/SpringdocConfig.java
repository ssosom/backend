package com.sosom.config;

import com.nimbusds.jwt.JWT;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@SecurityScheme(in = SecuritySchemeIn.HEADER,type = SecuritySchemeType.HTTP,scheme = "bearer",bearerFormat = "Authorization",name = HttpHeaders.AUTHORIZATION)
@OpenAPIDefinition(info = @Info(title = "sosom"))
public class SpringdocConfig {
}
