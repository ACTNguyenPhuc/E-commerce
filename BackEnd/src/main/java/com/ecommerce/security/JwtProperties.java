package com.ecommerce.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long accessTtlMinutes = 15;
    private long refreshTtlDays = 7;
    private String issuer = "ecommerce-backend";
}
