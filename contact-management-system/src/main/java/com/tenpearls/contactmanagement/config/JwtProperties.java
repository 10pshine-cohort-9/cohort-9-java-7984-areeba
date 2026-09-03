package com.tenpearls.contactmanagement.config;



import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.validation.annotation.Validated;



@ConfigurationProperties(prefix = "jwt")

@Validated

public class JwtProperties {



    @NotBlank(message = "JWT secret must be provided via JWT_SECRET environment variable")

    @Size(min = 32, message = "JWT secret must be at least 32 characters long")

    private String secret;



    @Min(value = 1, message = "JWT expiration must be greater than 0")

    private long expirationMs;


    public String getSecret() {

        return secret;

    }



    public void setSecret(String secret) {

        this.secret = secret;

    }



    public long getExpirationMs() {

        return expirationMs;

    }



    public void setExpirationMs(long expirationMs) {

        this.expirationMs = expirationMs;

    }

}


