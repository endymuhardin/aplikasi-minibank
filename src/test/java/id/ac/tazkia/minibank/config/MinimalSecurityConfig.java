package id.ac.tazkia.minibank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

@Configuration
public class MinimalSecurityConfig {

    @Bean
    public SecurityExpressionHandler<FilterInvocation> securityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }
}
