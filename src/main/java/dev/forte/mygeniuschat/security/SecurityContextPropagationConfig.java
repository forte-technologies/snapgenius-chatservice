package dev.forte.mygeniuschat.security;


import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;

@Configuration
public class SecurityContextPropagationConfig {
    @Bean
    public ThreadLocalAccessor<SecurityContext> securityContextAccessor() {
        return new SecurityContextThreadLocalAccessor();
    }

}